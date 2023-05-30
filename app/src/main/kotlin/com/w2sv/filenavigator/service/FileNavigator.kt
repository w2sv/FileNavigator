package com.w2sv.filenavigator.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.notifying.showNotification
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.mediastore.MediaStoreFileMetadata
import com.w2sv.filenavigator.mediastore.MediaType
import com.w2sv.kotlinutils.extensions.nonZeroOrdinal
import slimber.log.i

class FileNavigator : Service() {

    companion object {
        fun startService(context: Context) {
            context.startService(
                Intent(context, FileNavigator::class.java)
            )
            i { "Starting FileNavigator" }
        }

        fun getStopIntent(context: Context): Intent =
            Intent(context, FileNavigator::class.java)
                .setAction(ACTION_STOP_SERVICE)

        private const val ACTION_STOP_SERVICE = "com.w2sv.filetrafficnavigator.STOP"

        const val EXTRA_MEDIA_STORE_FILE_METADATA =
            "com.w2sv.filetrafficnavigator.extra.MEDIA_STORE_FILE_METADATA"
        const val EXTRA_NOTIFICATION_ID = "com.w2sv.filetrafficnavigator.extra.NOTIFICATION_ID"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val mediaTypeObservers: List<MediaTypeObserver> by lazy {
        MediaType.values().map { MediaTypeObserver(it) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                startForeground(
                    AppNotificationChannel.STARTED_FOREGROUND_SERVICE.nonZeroOrdinal,
                    createNotificationChannelAndGetNotificationBuilder(
                        AppNotificationChannel.STARTED_FOREGROUND_SERVICE,
                        getString(AppNotificationChannel.STARTED_FOREGROUND_SERVICE.titleRes)
                    )
                        .setContentText(getString(R.string.waiting_for_new_files_to_be_navigated))
                        // add 'Stop' action
                        .addAction(
                            NotificationCompat.Action(
                                R.drawable.ic_cancel_24,
                                getString(R.string.stop),
                                PendingIntent.getService(
                                    applicationContext,
                                    PendingIntentRequestCode.StopFileNavigator.ordinal,
                                    getStopIntent(applicationContext),
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                                ),
                            )
                        )
                        .build()
                )

                mediaTypeObservers.forEach {
                    contentResolver.registerContentObserver(
                        it.mediaType.mediaStoreUri,
                        true,
                        it
                    )
                }
                i { "Registered mediaTypeObservers" }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @Suppress("UnstableApiUsage")
    private inner class MediaTypeObserver(val mediaType: MediaType) :
        ContentObserver(Handler(Looper.getMainLooper())) {

        override fun deliverSelfNotifications(): Boolean = false

        private val mediaStoreFileDataBlacklist = EvictingQueue.create<MediaStoreFileMetadata>(5)

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)

            i { "Registered a new uri: $uri" }

            uri ?: return

            MediaStoreFileMetadata.fetch(uri, mediaType, contentResolver)
                ?.let { mediaStoreFileData ->
                    if (!mediaStoreFileData.isNewlyAdded || mediaStoreFileDataBlacklist.any {
                            it.pointsToSameContentAs(
                                mediaStoreFileData
                            )
                        })
                        return
                    val notificationContentText =
                        getString(
                            R.string.found_at,  // TODO: make bold
                            mediaStoreFileData.name,
                            mediaStoreFileData.relativePath
                        )

                    showNotification(
                        AppNotificationChannel.NEW_FILE_DETECTED.nonZeroOrdinal,
                        createNotificationChannelAndGetNotificationBuilder(
                            AppNotificationChannel.NEW_FILE_DETECTED,
                            getString(
                                AppNotificationChannel.NEW_FILE_DETECTED.titleRes,
                                mediaStoreFileData.mediaType.name
                            )
                        )
                            // set content
                            .setStyle(
                                NotificationCompat.BigTextStyle()
                                    .bigText(notificationContentText)
                            )
                            .setContentText(notificationContentText)
                            // add move action
                            .addAction(
                                NotificationCompat.Action(
                                    R.drawable.ic_file_move_24,
                                    getString(R.string.move),
                                    FileMoverActivity.getPendingIntent(
                                        applicationContext,
                                        mediaStoreFileData,
                                        AppNotificationChannel.NEW_FILE_DETECTED.nonZeroOrdinal
                                    )
                                )
                            )
                            // view file upon notification click
                            .setContentIntent(
                                PendingIntent.getActivity(
                                    this@FileNavigator,
                                    PendingIntentRequestCode.ViewImage.ordinal,
                                    Intent()
                                        .setAction(Intent.ACTION_VIEW)
                                        .setDataAndType(
                                            uri,
                                            mediaType.simpleStorageMediaType.mimeType
                                        ),
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                                )
                            )
                    )

                    mediaStoreFileDataBlacklist.add(mediaStoreFileData)
                }
        }
    }

    /**
     * Unregisters [mediaTypeObservers].
     */
    override fun onDestroy() {
        super.onDestroy()

        mediaTypeObservers.forEach {
            contentResolver.unregisterContentObserver(it)
        }
        i { "Unregistered mediaTypeObservers" }
    }
}