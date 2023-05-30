package com.w2sv.filenavigator.service

import android.app.Notification
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
import com.anggrayudi.storage.file.MimeType
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

        fun stopService(context: Context) {
            context.startService(
                getStopIntent(context)
            )
            i { "Stopping FileNavigator" }
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
                    getForegroundServiceNotification()
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

    private fun getForegroundServiceNotification(): Notification =
        applicationContext.createNotificationChannelAndGetNotificationBuilder(
            AppNotificationChannel.STARTED_FOREGROUND_SERVICE,
            AppNotificationChannel.STARTED_FOREGROUND_SERVICE.title
        )
            .setContentText("Waiting for new files to be navigated")
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_cancel_24,
                    "Stop",
                    PendingIntent.getService(
                        applicationContext,
                        PendingIntentRequestCode.StopFileNavigator.ordinal,
                        getStopIntent(applicationContext),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                    ),
                )
            )
            .build()

    @Suppress("UnstableApiUsage")
    private inner class MediaTypeObserver(val mediaType: MediaType) :
        ContentObserver(Handler(Looper.getMainLooper())) {

        private val mediaStoreFileDataBlacklist = EvictingQueue.create<MediaStoreFileMetadata>(5)

        override fun deliverSelfNotifications(): Boolean = false

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)

            i { "Registered new uri: $uri" }

            if (uri != null) {
                MediaStoreFileMetadata.fetch(uri, mediaType, contentResolver)
                    ?.let { mediaStoreFileData ->
                        if (mediaStoreFileData.isNewlyAdded && mediaStoreFileDataBlacklist.none {
                                it.pointsToSameContentAs(
                                    mediaStoreFileData
                                )
                            }) {
                            val notificationContentText =
                                "${mediaStoreFileData.name} found at ${mediaStoreFileData.relativePath}"

                            showNotification(
                                AppNotificationChannel.NEW_FILE_DETECTED.nonZeroOrdinal,
                                createNotificationChannelAndGetNotificationBuilder(
                                    AppNotificationChannel.NEW_FILE_DETECTED,
                                    AppNotificationChannel.NEW_FILE_DETECTED.title.format(
                                        mediaStoreFileData.mediaType.name
                                    )
                                )
                                    .setStyle(
                                        NotificationCompat.BigTextStyle()
                                            .bigText(notificationContentText)
                                    )
                                    .setContentText(notificationContentText)
                                    .addAction(
                                        NotificationCompat.Action(
                                            R.drawable.ic_file_move_24,
                                            "Move",
                                            FileMoverActivity.getPendingIntent(
                                                applicationContext,
                                                mediaStoreFileData,
                                                AppNotificationChannel.NEW_FILE_DETECTED.nonZeroOrdinal
                                            )
                                        )
                                    )
                                    .setContentIntent(
                                        PendingIntent.getActivity(
                                            this@FileNavigator,
                                            PendingIntentRequestCode.ViewImage.ordinal,
                                            Intent()
                                                .setAction(Intent.ACTION_VIEW)
                                                .setDataAndType(uri, MimeType.IMAGE),
                                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                                        )
                                    )
                            )
                            mediaStoreFileDataBlacklist.add(mediaStoreFileData)
                        }
                    }
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