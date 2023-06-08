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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.notifying.showNotification
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.DataStoreRepository
import com.w2sv.filenavigator.mediastore.FileMediaStoreData
import com.w2sv.filenavigator.mediastore.MediaStoreFile
import com.w2sv.filenavigator.mediastore.MediaType
import com.w2sv.filenavigator.utils.getSynchronousMap
import com.w2sv.filenavigator.utils.sendLocalBroadcast
import com.w2sv.kotlinutils.extensions.nonZeroOrdinal
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class FileListenerService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    private var mediaObservers: List<MediaObserver> = getMediaTypeObservers()

    private fun getMediaTypeObservers(): List<MediaObserver>{
        val accountForMediaType = dataStoreRepository.accountForMediaType.getSynchronousMap()
        val accountForMediaTypeOrigin =
            dataStoreRepository.accountForMediaTypeOrigin.getSynchronousMap()

        return MediaType.values()
            .filter { accountForMediaType.getValue(it) }
            .map { mediaType ->
                MediaObserver(
                    mediaType,
                    mediaType
                        .origins
                        .filter { origin -> accountForMediaTypeOrigin.getValue(origin) }
                        .map { origin -> origin.kind }
                        .toSet()
                )
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                sendLocalBroadcast(ACTION_FILE_LISTENER_SERVICE_STOPPED)
            }

            ACTION_REREGISTER_MEDIA_OBSERVERS -> {
                mediaObservers = getMediaTypeObservers()
            }

            else -> {
                startForeground(
                    AppNotificationChannel.STARTED_FOREGROUND_SERVICE.nonZeroOrdinal,
                    createNotificationChannelAndGetNotificationBuilder(
                        AppNotificationChannel.STARTED_FOREGROUND_SERVICE,
                        getString(AppNotificationChannel.STARTED_FOREGROUND_SERVICE.titleRes)
                    )
                        .setSmallIcon(R.drawable.ic_file_move_24)
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

                mediaObservers.forEach {
                    contentResolver.registerContentObserver(
                        it.mediaType.storageType.readUri!!,
                        true,
                        it
                    )
                }

                sendLocalBroadcast(ACTION_FILE_LISTENER_SERVICE_STARTED)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @Suppress("UnstableApiUsage")
    private inner class MediaObserver(
        val mediaType: MediaType,
        private val originKinds: Set<MediaType.OriginKind>
    ) :
        ContentObserver(Handler(Looper.getMainLooper())) {

        init {
            i { "Registered ${mediaType.name} MediaTypeObserver with originKinds: ${originKinds.map { it.name }}" }
        }

        override fun deliverSelfNotifications(): Boolean = false

        private val fileMediaStoreDataBlacklistCache = EvictingQueue.create<FileMediaStoreData>(5)

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)

            i { "Registered a new uri: $uri" }

            uri ?: return

            FileMediaStoreData.fetch(uri, contentResolver)?.let { mediaStoreData ->
                if (mediaStoreData.isPending) return@let

                if (mediaStoreData.isNewlyAdded &&
                    fileMediaStoreDataBlacklistCache.none {
                        it.pointsToSameContentAs(
                            mediaStoreData
                        )
                    } &&
                    originKinds.contains(mediaStoreData.getOriginKind())
                ) {
                    showNotification(
                        MediaStoreFile(
                            uri = uri,
                            type = mediaType,
                            mediaStoreData = mediaStoreData
                        )
                    )
                }

                fileMediaStoreDataBlacklistCache.add(mediaStoreData)
            }
        }

        private fun showNotification(mediaStoreFile: MediaStoreFile) {
            val notificationContentText =
                getString(
                    R.string.found_at,  // TODO: make bold
                    mediaStoreFile.mediaStoreData.name,
                    mediaStoreFile.mediaStoreData.relativePath
                )

            showNotification(
                AppNotificationChannel.NEW_FILE_DETECTED.nonZeroOrdinal,
                createNotificationChannelAndGetNotificationBuilder(
                    AppNotificationChannel.NEW_FILE_DETECTED,
                    getString(
                        AppNotificationChannel.NEW_FILE_DETECTED.titleRes,
                        mediaType.name
                    )
                )
                    .setSmallIcon(R.drawable.ic_file_move_24)
                    .setLargeIcon(
                        AppCompatResources.getDrawable(
                            this@FileListenerService,
                            mediaType.iconRes
                        )?.toBitmap()
                    )
                    // set content
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(notificationContentText)
                    )
                    .setContentText(notificationContentText)
                    // add move-file action
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_file_move_24,
                            getString(R.string.move),
                            FileMoverActivity.makePendingIntent(
                                applicationContext,
                                mediaStoreFile,
                                AppNotificationChannel.NEW_FILE_DETECTED.nonZeroOrdinal
                            )
                        )
                    )
                    // add open-file action
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_file_open_24,
                            getString(R.string.open),
                            PendingIntent.getActivity(
                                this@FileListenerService,
                                PendingIntentRequestCode.OpenFile.ordinal,
                                Intent()
                                    .setAction(Intent.ACTION_VIEW)
                                    .setDataAndType(
                                        mediaStoreFile.uri,
                                        mediaType.storageType.mimeType
                                    ),
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                            )
                        )
                    )
            )
        }
    }

    /**
     * Unregisters [mediaObservers].
     */
    override fun onDestroy() {
        super.onDestroy()

        mediaObservers.forEach {
            contentResolver.unregisterContentObserver(it)
        }
        i { "Unregistered mediaTypeObservers" }
    }

    companion object {
        fun start(context: Context) {
            context.startService(
                Intent(context, FileListenerService::class.java)
            )
            i { "Starting FileNavigator" }
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, FileListenerService::class.java)
                    .setAction(ACTION_STOP_SERVICE)
            )
            i { "Stopping FileNavigator" }
        }

        fun reregisterMediaObservers(context: Context){
            context.startService(
                Intent(context, FileListenerService::class.java)
                    .setAction(ACTION_REREGISTER_MEDIA_OBSERVERS)
            )
            i { "Reregistering MediaObservers" }
        }

        fun getStopIntent(context: Context): Intent =
            Intent(context, FileListenerService::class.java)
                .setAction(ACTION_STOP_SERVICE)

        private const val ACTION_STOP_SERVICE = "com.w2sv.filenavigator.STOP"

        const val EXTRA_MEDIA_STORE_FILE =
            "com.w2sv.filenavigator.extra.MEDIA_STORE_FILE"
        const val EXTRA_NOTIFICATION_ID = "com.w2sv.filenavigator.extra.NOTIFICATION_ID"

        const val ACTION_FILE_LISTENER_SERVICE_STARTED =
            "com.w2sv.filenavigator.FILE_LISTENER_SERVICE_STARTED"
        const val ACTION_FILE_LISTENER_SERVICE_STOPPED =
            "com.w2sv.filenavigator.FILE_LISTENER_SERVICE_STOPPED"
        const val ACTION_REREGISTER_MEDIA_OBSERVERS =
            "com.w2sv.filenavigator.REREGISTER_MEDIA_OBSERVERS"
    }
}