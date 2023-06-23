package com.w2sv.filenavigator.navigator.service

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
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.UniqueIds
import com.w2sv.androidutils.notifying.showNotification
import com.w2sv.androidutils.services.UnboundService
import com.w2sv.filenavigator.FileType
import com.w2sv.filenavigator.MainActivity
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.PreferencesDataStoreRepository
import com.w2sv.filenavigator.navigator.MoveFile
import com.w2sv.filenavigator.navigator.mediastore.MediaStoreFileData
import com.w2sv.filenavigator.navigator.notifications.AppNotificationChannel
import com.w2sv.filenavigator.navigator.notifications.PendingIntentRequestCode
import com.w2sv.filenavigator.navigator.notifications.createNotificationChannelAndGetNotificationBuilder
import com.w2sv.filenavigator.utils.sendLocalBroadcast
import com.w2sv.kotlinutils.extensions.nonZeroOrdinal
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class FileNavigatorService : UnboundService() {

    @Inject
    lateinit var dataStoreRepository: PreferencesDataStoreRepository

    private lateinit var fileObservers: List<FileObserver>

    private val newFileDetectedNotificationIds =
        UniqueIds(AppNotificationChannel.NewFileDetected.idGroupSeed)
    private val newFileDetectedActionsPendingIntentRequestCodes = UniqueIds(1)

    private fun getAndRegisterFileObservers(): List<FileObserver> {
        val fileTypeStatus = dataStoreRepository.fileTypeStatus.getSynchronousMap()
        val accountForFileTypeOrigin =
            dataStoreRepository.mediaFileSourceEnabled.getSynchronousMap()

        val mediaFileObservers = FileType.Media.all
            .filter { fileTypeStatus.getValue(it.status) == FileType.Status.Enabled }
            .map { mediaType ->
                MediaFileObserver(
                    mediaType,
                    mediaType
                        .sources
                        .filter { origin -> accountForFileTypeOrigin.getValue(origin.isEnabled) }
                        .map { origin -> origin.kind }
                        .toSet()
                )
            }

        val nonMediaFileObserver =
            FileType.NonMedia.all.filter { fileTypeStatus.getValue(it.status) == FileType.Status.Enabled }
                .run {
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
                stop()
            }

            ACTION_REREGISTER_MEDIA_OBSERVERS -> {
                unregisterContentObservers()
                fileObservers = getAndRegisterFileObservers()
            }

            ACTION_CLEANUP_IDS -> {
                val notificationParameters =
                    intent.getParcelableCompat<MoveFile.NotificationParameters>(MoveFile.NotificationParameters.EXTRA)!!

                newFileDetectedNotificationIds.remove(notificationParameters.notificationId)
                newFileDetectedActionsPendingIntentRequestCodes.removeAll(notificationParameters.requestCodes.toSet())
            }

            else -> try {
                start()
            } catch (e: RuntimeException) {
                i(e)
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        startForeground(
            AppNotificationChannel.StartedForegroundService.nonZeroOrdinal,
            createNotificationChannelAndGetNotificationBuilder(
                AppNotificationChannel.StartedForegroundService
            )
                .setSmallIcon(R.drawable.ic_file_move_24)
                .setContentTitle(getString(AppNotificationChannel.StartedForegroundService.titleRes))
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

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        sendLocalBroadcast(ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED)
    }

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

        protected fun showNotification(moveFile: MoveFile) {
            val notificationContentText =
                getString(
                    R.string.found_at,
                    moveFile.data.name,
                    moveFile.data.relativePath
                )

            val notificationParameters = MoveFile.NotificationParameters(
                newFileDetectedNotificationIds.addNewId(),
                newFileDetectedActionsPendingIntentRequestCodes.addMultipleNewIds(2)
            )

            showNotification(
                notificationParameters.notificationId,
                createNotificationChannelAndGetNotificationBuilder(
                    AppNotificationChannel.NewFileDetected
                )
                    .setContentTitle(
                        getString(
                            AppNotificationChannel.NewFileDetected.titleRes,
                            getNotificationTitleFormatArg(moveFile)
                        )
                    )
                    // set icons
                    .setSmallIcon(moveFile.type.iconRes)
                    .setLargeIcon(
                        AppCompatResources.getDrawable(
                            applicationContext,
                            moveFile.sourceKind.iconRes
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
                                notificationParameters.requestCodes[0],
                                Intent.makeRestartActivityTask(
                                    ComponentName(
                                        applicationContext,
                                        FileMoverActivity::class.java
                                    )
                                )
                                    .putExtra(EXTRA_MOVE_FILE, moveFile)
                                    .putExtra(
                                        MoveFile.NotificationParameters.EXTRA,
                                        notificationParameters
                                    ),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    )
                    // add open-file action
                    .addAction(
                        NotificationCompat.Action(
                            R.drawable.ic_file_open_24,
                            getString(R.string.view),
                            PendingIntent.getActivity(
                                applicationContext,
                                notificationParameters.requestCodes[1],
                                Intent()
                                    .setAction(Intent.ACTION_VIEW)
                                    .setDataAndType(
                                        moveFile.uri,
                                        moveFile.type.simpleStorageType.mimeType
                                    ),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    )
            )
        }

        protected abstract fun getNotificationTitleFormatArg(moveFile: MoveFile): String
    }

    private inner class MediaFileObserver(
        private val fileType: FileType.Media,
        private val sourceKinds: Set<FileType.SourceKind>
    ) :
        FileObserver(fileType.simpleStorageType.readUri!!) {

        init {
            i { "Initialized ${fileType::class.java.simpleName} MediaTypeObserver with originKinds: ${sourceKinds.map { it.name }}" }
        }

        override fun showNotificationIfApplicable(
            uri: Uri,
            mediaStoreFileData: MediaStoreFileData
        ) {
            if (fileType.matchesFileExtension(mediaStoreFileData.fileExtension)) {
                val sourceKind = mediaStoreFileData.getSourceKind()

                if (sourceKinds.contains(sourceKind)) {
                    showNotification(
                        MoveFile(
                            uri = uri,
                            type = fileType,
                            sourceKind = sourceKind,
                            data = mediaStoreFileData
                        )
                    )
                }
            }
        }

        override fun getNotificationTitleFormatArg(moveFile: MoveFile): String {
            moveFile.type as FileType.Media

            return when (moveFile.data.getSourceKind()) {
                FileType.SourceKind.Screenshot -> getString(
                    R.string.new_screenshot
                )

                FileType.SourceKind.Camera -> getString(
                    when (moveFile.type) {
                        FileType.Media.Image -> R.string.new_photo
                        FileType.Media.Video -> R.string.new_video
                        else -> throw Error()
                    }
                )

                FileType.SourceKind.Download -> getString(
                    R.string.newly_downloaded_template,
                    getString(moveFile.type.fileDeclarationRes)
                )

                FileType.SourceKind.OtherApp -> getString(
                    R.string.new_third_party_file_template,
                    moveFile.data.dirName,
                    getString(moveFile.type.fileDeclarationRes)
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
            fileTypes.firstOrNull { it.matchesFileExtension(mediaStoreFileData.fileExtension) }
                ?.let { fileType ->
                    showNotification(
                        MoveFile(
                            uri = uri,
                            type = fileType,
                            sourceKind = FileType.SourceKind.Download,
                            data = mediaStoreFileData
                        )
                    )
                }
        }

        override fun getNotificationTitleFormatArg(moveFile: MoveFile): String =
            getString(R.string.new_file, getString(moveFile.type.titleRes))
    }

    private fun unregisterContentObservers() {
        fileObservers.forEach {
            contentResolver.unregisterContentObserver(it)
        }
        i { "Unregistered fileObservers" }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterContentObservers()
        } catch (e: UninitializedPropertyAccessException) {
            i(e)
        }
    }

    companion object {
        fun start(context: Context) {
            context.startService(
                getIntent(context)
            )
        }

        fun stop(context: Context) {
            context.startService(
                getStopIntent(context)
            )
        }

        fun onNotificationCancelled(
            notificationParameters: MoveFile.NotificationParameters,
            context: Context
        ) {
            context.startService(
                getIntent(context)
                    .setAction(ACTION_CLEANUP_IDS)
                    .putExtra(MoveFile.NotificationParameters.EXTRA, notificationParameters)
            )
        }

        fun reregisterFileObservers(context: Context) {
            context.startService(
                getIntent(context)
                    .setAction(ACTION_REREGISTER_MEDIA_OBSERVERS)
            )
        }

        fun getStopIntent(context: Context): Intent =
            getIntent(context)
                .setAction(ACTION_STOP_SERVICE)

        private fun getIntent(context: Context): Intent =
            Intent(context, FileNavigatorService::class.java)

        const val EXTRA_MOVE_FILE =
            "com.w2sv.filenavigator.extra.MOVE_FILE"

        const val ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED =
            "com.w2sv.filenavigator.NOTIFY_FILE_LISTENER_SERVICE_STARTED"
        const val ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED =
            "com.w2sv.filenavigator.NOTIFY_FILE_LISTENER_SERVICE_STOPPED"
        const val ACTION_REREGISTER_MEDIA_OBSERVERS =
            "com.w2sv.filenavigator.REREGISTER_MEDIA_OBSERVERS"
        const val ACTION_CLEANUP_IDS = "com.w2sv.filenavigator.CLEANUP_IDS"
        const val ACTION_STOP_SERVICE = "com.w2sv.filenavigator.STOP"
    }
}