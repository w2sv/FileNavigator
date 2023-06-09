package com.w2sv.filenavigator.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.anggrayudi.storage.media.MediaType
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.notifying.showNotification
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.DataStoreRepository
import com.w2sv.filenavigator.mediastore.FileType
import com.w2sv.filenavigator.mediastore.MediaStoreFile
import com.w2sv.filenavigator.mediastore.MediaStoreFileData
import com.w2sv.filenavigator.ui.screens.main.MainActivity
import com.w2sv.filenavigator.utils.getSynchronousMap
import com.w2sv.filenavigator.utils.sendLocalBroadcast
import com.w2sv.kotlinutils.extensions.nonZeroOrdinal
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class FileListenerService : UnboundService() {

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    private lateinit var fileObservers: List<FileObserver>

    private val newFileDetectedNotificationIds =
        IdGroup(AppNotificationChannel.NEW_FILE_DETECTED.nonZeroOrdinal)
    private val newFileDetectedActionsPendingIntentRequestCodes = IdGroup(1)

    private fun getAndRegisterFileObservers(): List<FileObserver> {
        val accountForFileType = dataStoreRepository.accountForFileType.getSynchronousMap()
        val accountForFileTypeOrigin =
            dataStoreRepository.accountForFileTypeOrigin.getSynchronousMap()

        val mediaFileObservers = FileType.Media.all
            .filter { accountForFileType.getValue(it) }
            .map { mediaType ->
                MediaFileObserver(
                    mediaType,
                    mediaType
                        .origins
                        .filter { origin -> accountForFileTypeOrigin.getValue(origin) }
                        .map { origin -> origin.kind }
                        .toSet()
                )
            }

        val nonMediaFileObserver =
            FileType.NonMedia.all.filter { accountForFileType.getValue(it) }.run {
                if (isNotEmpty()) {
                    NonMediaFileObserver(this)
                } else {
                    null
                }
            }

        return buildList {
            addAll(mediaFileObservers)
            nonMediaFileObserver?.let {
                add(it)
            }
        }
            .onEach {
                contentResolver.registerContentObserver(
                    it.contentObserverUri,
                    true,
                    it
                )
            }
            .also { i { "Registered ${it.size} FileObservers" } }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        i { "onStartCommand | action: ${intent?.action}" }

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                sendLocalBroadcast(ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED)
            }

            ACTION_REREGISTER_MEDIA_OBSERVERS -> {
                unregisterContentObservers()
                fileObservers = getAndRegisterFileObservers()
            }

            ACTION_CLEANUP_IDS -> {
                newFileDetectedNotificationIds.remove(intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1))
                newFileDetectedActionsPendingIntentRequestCodes.removeAll(
                    intent.getIntegerArrayListExtra(
                        EXTRA_REQUEST_CODES
                    )!!
                        .toSet()
                )
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

                fileObservers = getAndRegisterFileObservers()

                sendLocalBroadcast(ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @Suppress("UnstableApiUsage")
    private abstract inner class FileObserver(val contentObserverUri: Uri) :
        ContentObserver(Handler(Looper.getMainLooper())) {

        override fun deliverSelfNotifications(): Boolean = false

        private val mediaStoreFileDataBlacklistCache = EvictingQueue.create<MediaStoreFileData>(5)

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)

            i { "onChange | Uri: $uri" }

            uri ?: return

            MediaStoreFileData.fetch(uri, contentResolver)?.let { mediaStoreFileData ->
                if (mediaStoreFileData.isPending) return@let

                if (mediaStoreFileData.isNewlyAdded &&
                    mediaStoreFileDataBlacklistCache.none {
                        it.pointsToSameContentAs(
                            mediaStoreFileData
                        )
                    }
                ) {
                    showNotificationIfApplicable(uri, mediaStoreFileData)
                }

                mediaStoreFileDataBlacklistCache.add(mediaStoreFileData)
            }
        }

        protected abstract fun showNotificationIfApplicable(
            uri: Uri,
            mediaStoreFileData: MediaStoreFileData
        )

        protected fun showNotification(mediaStoreFile: MediaStoreFile) {
            val notificationContentText =
                getString(
                    R.string.found_at,
                    mediaStoreFile.data.name,
                    mediaStoreFile.data.relativePath
                )

            val notificationId = newFileDetectedNotificationIds.addNewId()
            val requestCodes =
                newFileDetectedActionsPendingIntentRequestCodes.addMultipleNewIds(2)

            showNotification(
                notificationId,
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
                            applicationContext,
                            mediaStoreFile.type.iconRes
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
                            PendingIntent.getActivity(
                                applicationContext,
                                requestCodes[0],
                                Intent.makeRestartActivityTask(
                                    ComponentName(
                                        applicationContext,
                                        FileMoverActivity::class.java
                                    )
                                )
                                    .putExtra(EXTRA_MEDIA_STORE_FILE, mediaStoreFile)
                                    .putExtra(
                                        EXTRA_NOTIFICATION_ID,
                                        notificationId
                                    )
                                    .putExtra(
                                        EXTRA_REQUEST_CODES,
                                        requestCodes
                                    ),
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                            )
                        )
                    )
                    // add open-file action
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_file_open_24,
                            getString(R.string.open),
                            PendingIntent.getActivity(
                                applicationContext,
                                requestCodes[1],
                                Intent()
                                    .setAction(Intent.ACTION_VIEW)
                                    .setDataAndType(
                                        mediaStoreFile.uri,
                                        mediaStoreFile.type.storageType.mimeType
                                    ),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    )
            )
        }

        protected abstract fun getNotificationTitleFormatArg(mediaStoreFile: MediaStoreFile): String
    }

    private inner class MediaFileObserver(
        private val mediaType: FileType,
        private val originKinds: Set<FileType.OriginKind>
    ) :
        FileObserver(mediaType.storageType.readUri!!) {

        init {
            i { "Initialized ${mediaType::class.java.simpleName} MediaTypeObserver with originKinds: ${originKinds.map { it.name }}" }
        }

        override fun showNotificationIfApplicable(
            uri: Uri,
            mediaStoreFileData: MediaStoreFileData
        ) {
            if (originKinds.contains(mediaStoreFileData.originKind)) {
                showNotification(
                    MediaStoreFile(
                        uri = uri,
                        type = mediaType,
                        data = mediaStoreFileData
                    )
                )
            }
        }

        override fun getNotificationTitleFormatArg(mediaStoreFile: MediaStoreFile): String {
            mediaStoreFile.type as FileType.Media

            return when (mediaStoreFile.data.originKind) {
                FileType.OriginKind.Screenshot -> getString(
                    R.string.new_screenshot
                )

                FileType.OriginKind.Camera -> getString(
                    when (mediaStoreFile.type) {
                        FileType.Image -> R.string.new_photo
                        FileType.Video -> R.string.new_video
                        else -> throw Error()
                    }
                )

                FileType.OriginKind.Download -> getString(
                    R.string.newly_downloaded_template,
                    getString(mediaStoreFile.type.fileDeclarationRes)
                )

                FileType.OriginKind.ThirdPartyApp -> getString(
                    R.string.new_third_party_file_template,
                    mediaStoreFile.data.dirName,
                    getString(mediaStoreFile.type.fileDeclarationRes)
                )
            }
        }
    }

    private inner class NonMediaFileObserver(private val fileTypes: List<FileType.NonMedia>) :
        FileObserver(MediaType.DOWNLOADS.readUri!!) {

        init {
            i { "Initialized NonMediaFileObserver with fileTypes: ${fileTypes.map { it::class.java.simpleName }}" }
        }

        override fun showNotificationIfApplicable(
            uri: Uri,
            mediaStoreFileData: MediaStoreFileData
        ) {
            fileTypes.firstOrNull { it.fileExtension == mediaStoreFileData.fileExtension }
                ?.let { fileType ->
                    showNotification(
                        MediaStoreFile(
                            uri = uri,
                            type = fileType,
                            data = mediaStoreFileData
                        )
                    )
                }
        }

        override fun getNotificationTitleFormatArg(mediaStoreFile: MediaStoreFile): String =
            getString(R.string.new_file, getString(mediaStoreFile.type.titleRes))
    }

    private fun unregisterContentObservers() {
        fileObservers.forEach {
            contentResolver.unregisterContentObserver(it)
        }
        i { "Unregistered mediaTypeObservers" }
    }

    /**
     * Unregisters [fileObservers].
     */
    override fun onDestroy() {
        super.onDestroy()

        unregisterContentObservers()
    }

    companion object {
        fun start(context: Context) {
            context.startService(
                Intent(context, FileListenerService::class.java)
            )
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, FileListenerService::class.java)
                    .setAction(ACTION_STOP_SERVICE)
            )
        }

        fun cleanUpIds(notificationId: Int, requestCodes: ArrayList<Int>, context: Context) {
            context.startService(
                Intent(context, FileListenerService::class.java)
                    .setAction(ACTION_CLEANUP_IDS)
                    .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                    .putExtra(EXTRA_REQUEST_CODES, requestCodes)
            )
        }

        fun reregisterMediaObservers(context: Context) {
            context.startService(
                Intent(context, FileListenerService::class.java)
                    .setAction(ACTION_REREGISTER_MEDIA_OBSERVERS)
            )
        }

        fun getStopIntent(context: Context): Intent =
            Intent(context, FileListenerService::class.java)
                .setAction(ACTION_STOP_SERVICE)

        const val EXTRA_MEDIA_STORE_FILE =
            "com.w2sv.filenavigator.extra.MEDIA_STORE_FILE"
        const val EXTRA_NOTIFICATION_ID = "com.w2sv.filenavigator.extra.NOTIFICATION_ID"
        const val EXTRA_REQUEST_CODES =
            "com.w2sv.filenavigator.extra.ASSOCIATED_REQUEST_CODES"

        const val ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED =
            "com.w2sv.filenavigator.NOTIFY_FILE_LISTENER_SERVICE_STARTED"
        const val ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED =
            "com.w2sv.filenavigator.NOTIFY_FILE_LISTENER_SERVICE_STOPPED"
        const val ACTION_REREGISTER_MEDIA_OBSERVERS =
            "com.w2sv.filenavigator.REREGISTER_MEDIA_OBSERVERS"
        const val ACTION_CLEANUP_IDS = "com.w2sv.filenavigator.CLEANUP_IDS"
        private const val ACTION_STOP_SERVICE = "com.w2sv.filenavigator.STOP"
    }
}