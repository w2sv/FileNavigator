package com.w2sv.filenavigator.service

import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
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
import com.w2sv.filenavigator.mediastore.MediaStoreFile
import com.w2sv.filenavigator.mediastore.MediaStoreFileData
import com.w2sv.filenavigator.mediastore.MediaType
import com.w2sv.filenavigator.ui.screens.main.MainActivity
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

    private lateinit var mediaObservers: List<MediaObserver>

    private fun getMediaTypeObservers(): List<MediaObserver> {
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
                sendLocalBroadcast(ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED)
            }

            ACTION_REREGISTER_MEDIA_OBSERVERS -> {
                mediaObservers = getMediaTypeObservers()
            }

            else -> {
                startForeground(
                    AppNotificationChannel.STARTED_FOREGROUND_SERVICE.nonZeroOrdinal,
                    createNotificationChannelAndGetNotificationBuilder(
                        AppNotificationChannel.STARTED_FOREGROUND_SERVICE
                    )
                        .setSmallIcon(R.drawable.ic_file_move_24)
                        .setContentTitle(getString(AppNotificationChannel.STARTED_FOREGROUND_SERVICE.titleRes))
                        .setContentText(getString(R.string.waiting_for_new_files_to_be_navigated))
                        // add configure action
                        .addAction(
                            NotificationCompat.Action(
                                R.drawable.ic_settings_24,
                                getString(R.string.configure),
                                PendingIntent.getActivity(
                                    applicationContext,
                                    PendingIntentRequestCode.ConfigureFileNavigator.ordinal,
                                    Intent.makeRestartActivityTask(
                                        ComponentName(this, MainActivity::class.java)
                                    ),
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            )
                        )
                        // add stop action
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

                mediaObservers = getMediaTypeObservers()
                    .onEach {
                        contentResolver.registerContentObserver(
                            it.mediaType.storageType.readUri!!,
                            true,
                            it
                        )
                    }

                sendLocalBroadcast(ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED)
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

        private val mediaStoreFileDataBlacklistCache = EvictingQueue.create<MediaStoreFileData>(5)

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)

            i { "Registered a new uri: $uri" }

            uri ?: return

            MediaStoreFileData.fetch(uri, contentResolver)?.let { mediaStoreData ->
                if (mediaStoreData.isPending) return@let

                if (mediaStoreData.isNewlyAdded &&
                    mediaStoreFileDataBlacklistCache.none {
                        it.pointsToSameContentAs(
                            mediaStoreData
                        )
                    } &&
                    originKinds.contains(mediaStoreData.originKind)
                ) {
                    showNotification(
                        MediaStoreFile(
                            uri = uri,
                            type = mediaType,
                            data = mediaStoreData
                        )
                    )
                }

                mediaStoreFileDataBlacklistCache.add(mediaStoreData)
            }
        }

        private fun showNotification(mediaStoreFile: MediaStoreFile) {
            val notificationContentText =
                getString(
                    R.string.found_at,
                    mediaStoreFile.data.name,
                    mediaStoreFile.data.relativePath
                )

            showNotification(
                AppNotificationChannel.NEW_FILE_DETECTED.nonZeroOrdinal,
                createNotificationChannelAndGetNotificationBuilder(
                    AppNotificationChannel.NEW_FILE_DETECTED
                )
                    .setContentTitle(
                        getString(
                            AppNotificationChannel.NEW_FILE_DETECTED.titleRes,
                            getNotificationTitleFormatArg(mediaStoreFile)
                        )
                    )
                    // set icons
                    .setSmallIcon(R.drawable.ic_file_move_24)
                    .setLargeIcon(
                        AppCompatResources.getDrawable(
                            this@FileListenerService,
                            mediaType.iconRes
                        )
                            ?.toBitmap()
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
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    )
            )
        }

        fun getNotificationTitleFormatArg(mediaStoreFile: MediaStoreFile): String =
            when (mediaStoreFile.data.originKind) {
                MediaType.OriginKind.Screenshot -> getString(
                    R.string.new_screenshot
                )

                MediaType.OriginKind.Camera -> getString(
                    when (mediaStoreFile.type) {
                        MediaType.Image -> R.string.new_photo
                        MediaType.Video -> R.string.new_video
                        else -> throw Error()
                    }
                )

                MediaType.OriginKind.Download -> getString(
                    R.string.newly_downloaded_template,
                    getString(mediaStoreFile.type.fileLabelRes)
                )

                MediaType.OriginKind.ThirdPartyApp -> getString(
                    R.string.new_third_party_file_template,
                    mediaStoreFile.data.dirName,
                    getString(mediaStoreFile.type.fileLabelRes)
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

        fun reregisterMediaObservers(context: Context) {
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

        const val ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED =
            "com.w2sv.filenavigator.NOTIFY_FILE_LISTENER_SERVICE_STARTED"
        const val ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED =
            "com.w2sv.filenavigator.NOTIFY_FILE_LISTENER_SERVICE_STOPPED"
        const val ACTION_REREGISTER_MEDIA_OBSERVERS =
            "com.w2sv.filenavigator.REREGISTER_MEDIA_OBSERVERS"
    }
}