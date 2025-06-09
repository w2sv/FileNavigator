package com.w2sv.navigator.notifications.appnotifications.movefile

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.text.SpannedString
import android.util.Size
import androidx.core.app.NotificationCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.util.formattedFileSize
import com.w2sv.common.util.lineBreakSuffixed
import com.w2sv.common.util.loadBitmapWithFileNotFoundHandling
import com.w2sv.common.util.log
import com.w2sv.common.util.removeSlashSuffix
import com.w2sv.common.util.slashPrefixed
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.flow.stateInWithBlockingInitial
import com.w2sv.navigator.moving.api.activity.AbstractDestinationPickerActivity
import com.w2sv.navigator.moving.model.DestinationSelectionManner
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveDestination
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.quick.QuickMoveDestinationAccessPermissionQueryActivity
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.api.MultiInstanceNotificationManager
import com.w2sv.navigator.notifications.api.SummarizedMultiInstanceNotificationManager
import com.w2sv.navigator.notifications.appnotifications.AppNotificationChannel
import com.w2sv.navigator.notifications.appnotifications.AppNotificationId
import com.w2sv.navigator.notifications.appnotifications.batchmove.BatchMoveNotificationManager
import com.w2sv.navigator.notifications.appnotifications.iconBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import slimber.log.i

@Singleton
internal class MoveFileNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager,
    navigatorConfigDataSource: NavigatorConfigDataSource,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    private val batchMoveNotificationManager: BatchMoveNotificationManager
) : SummarizedMultiInstanceNotificationManager<MoveFileNotificationManager.Args>(
    appNotificationChannel = AppNotificationChannel.NewNavigatableFile,
    notificationManager = notificationManager,
    context = context,
    appNotificationId = AppNotificationId.NewNavigatableFile
) {
    data class Args(
        val moveFile: MoveFile,
        val quickMoveDestinations: List<MoveDestination.Directory>,
        override val notificationResources: NotificationResources
    ) : MultiInstanceNotificationManager.Args

    fun buildAndPostNotification(moveFile: MoveFile) {
        buildAndPostNotification(
            Args(
                moveFile = moveFile,
                quickMoveDestinations = fileAndSourceTypeToQuickMoveDestinationStateFlow.quickMoveDestinations(
                    moveFile.fileAndSourceType
                )
                    .log { "Retrieved quickMoveDestination: $it" },
                notificationResources = getNotificationResources()
            )
        )
    }

    private val fileAndSourceTypeToQuickMoveDestinationStateFlow =
        FileAndSourceTypeToQuickMoveDestinationStateFlow(navigatorConfigDataSource, scope)

    private val showBatchMoveNotification =
        navigatorConfigDataSource
            .navigatorConfig
            .map { it.showBatchMoveNotification }
            .stateIn(scope, SharingStarted.Eagerly, false)

    private val notificationIdToArgs = mutableMapOf<Int, Args>()

    private fun buildAndPostNotification(args: Args) {
        super.buildAndPost(args)

        notificationIdToArgs[args.notificationResources.id] = args

        if (activeNotificationCount >= 2 && showBatchMoveNotification.value) {
            batchMoveNotificationManager.buildAndPostNotification(notificationIdToArgs.values)
        }
    }

    override fun cancelNotification(id: Int) {
        super.cancelNotification(id)

        notificationIdToArgs.remove(id)
        if (showBatchMoveNotification.value) {
            batchMoveNotificationManager.cancelOrUpdate(notificationIdToArgs.values)
        }
    }

    override fun getBuilder(args: Args): Builder =
        object : Builder() {

            override fun build(): Notification {
                setContentTitle(args.moveFile.notificationTitle(context))
                setLargeIcon(args.moveFile.largeNotificationIcon(context))

                setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(args.moveFile.notificationContentText(context))
                )
                setActionsAndIntents()

                return super.build()
            }

            private fun setActionsAndIntents() {
                // Set actions & intents
                val requestCodeIterator = args.notificationResources.pendingIntentRequestCodes(5).iterator()

                addAction(getMoveFileAction(requestCodeIterator.next()))

                // Add quickMoveAction if quickMoveDestination present.
                args.quickMoveDestinations.firstOrNull()?.let { quickMoveDestination ->
                    i { "quickMoveDestination=$quickMoveDestination" }

                    // TODO: checking whether destination exists might be possible by querying the abs path through media uri and converting to a java.io.File
                    addAction(
                        getQuickMoveAction(
                            requestCode = requestCodeIterator.next(),
                            destination = quickMoveDestination,
                            directoryName = quickMoveDestination.fileName(context)
                        )
                    )
                }

                addAction(getDeleteFileAction(requestCodeIterator.next()))

                setContentIntent(getViewFilePendingIntent(requestCodeIterator.next()))

                setDeleteIntent(
                    getCleanupNotificationResourcesPendingIntent(
                        requestCode = requestCodeIterator.next(),
                        notificationResources = args.notificationResources
                    )
                )
            }

            private fun getViewFilePendingIntent(requestCode: Int): PendingIntent =
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    ViewFileIfPresentActivity.makeRestartActivityIntent(
                        context = context,
                        args = ViewFileIfPresentActivity.Args(args.moveFile),
                        notificationResources = args.notificationResources
                    ),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

            private fun getMoveFileAction(requestCode: Int): NotificationCompat.Action =
                NotificationCompat.Action(
                    com.w2sv.core.common.R.drawable.ic_app_logo_24,
                    context.getString(R.string.move),
                    PendingIntent.getActivity(
                        context,
                        requestCode,
                        AbstractDestinationPickerActivity.makeRestartActivityIntent<FileDestinationPickerActivity>(
                            args = FileDestinationPickerActivity.Args(
                                moveFile = args.moveFile,
                                pickerStartDestination = args.quickMoveDestinations.firstOrNull()?.documentUri,
                                notificationResources = args.notificationResources
                            ),
                            context = context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

            private fun getQuickMoveAction(
                requestCode: Int,
                destination: MoveDestination.Directory,
                directoryName: String
            ): NotificationCompat.Action =
                NotificationCompat.Action(
                    com.w2sv.core.common.R.drawable.ic_app_logo_24,
                    context.getString(R.string.to, directoryName),
                    PendingIntent.getActivity(
                        context,
                        requestCode,
                        QuickMoveDestinationAccessPermissionQueryActivity.makeRestartActivityTaskIntent(
                            MoveBundle.QuickMove(
                                file = args.moveFile,
                                destination = destination,
                                destinationSelectionManner = DestinationSelectionManner.Quick(args.notificationResources),
                                batched = false
                            ),
                            context = context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

            private fun getDeleteFileAction(requestCode: Int): NotificationCompat.Action {
                return NotificationCompat.Action(
                    R.drawable.ic_delete_24,
                    context.getString(R.string.delete),
                    PendingIntent.getActivity(
                        context,
                        requestCode,
                        FileDeletionActivity.getIntent(args.moveFile, args.notificationResources, context),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }
        }

    override fun buildSummaryNotification(): Notification =
        Builder()
            .setContentTitle(
                context.resources.getQuantityString(
                    R.plurals.navigatable_file,
                    activeNotificationCount,
                    activeNotificationCount
                )
            )
            .setSilent(true)
            .setGroupSummary(true)
            .build()
}

private class FileAndSourceTypeToQuickMoveDestinationStateFlow(
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val scope: CoroutineScope,
    private val mutableMap: MutableMap<FileAndSourceType, StateFlow<List<MoveDestination.Directory>>> = mutableMapOf()
) : Map<FileAndSourceType, StateFlow<List<MoveDestination.Directory>>> by mutableMap {

    fun quickMoveDestinations(fileAndSourceType: FileAndSourceType): List<MoveDestination.Directory> =
        mutableMap.getOrPut(
            key = fileAndSourceType,
            defaultValue = {
                navigatorConfigDataSource
                    .quickMoveDestinations(
                        fileType = fileAndSourceType.fileType,
                        sourceType = fileAndSourceType.sourceType
                    )
                    .map { it.map { localDestinationApi -> MoveDestination.Directory(localDestinationApi) } }
                    .stateInWithBlockingInitial(scope)
            }
        )
            .value
}

private fun MoveFile.largeNotificationIcon(context: Context): Bitmap? =
    when (fileType.wrappedPresetTypeOrNull) {
        PresetFileType.Image -> context.contentResolver.loadBitmapWithFileNotFoundHandling(
            mediaUri.uri
        )

        PresetFileType.Video -> {
            try {
                context.contentResolver.loadThumbnail(
                    mediaUri.uri,
                    Size(256, 256),
                    null
                )
            } catch (_: IOException) {
                null
            }
        }

        else -> null
    } ?: fileAndSourceType.iconBitmap(context)

private fun MoveFile.notificationContentText(context: Context): SpannedString =
    buildSpannedString {
        append(mediaStoreFileData.name.lineBreakSuffixed())
        bold { append(context.getString(R.string.directory).lineBreakSuffixed()) }
        append(
            mediaStoreFileData.volumeRelativeDirPath
                .removeSlashSuffix()
                .slashPrefixed()
                .lineBreakSuffixed()
        )
        bold { append(context.getString(R.string.size).lineBreakSuffixed()) }
        append(formattedFileSize(mediaStoreFileData.size))
    }

private fun MoveFile.notificationTitle(context: Context): String =
    context.getString(
        R.string.new_move_file_notification_title,
        notificationLabel(context = context)
    )

private fun MoveFile.notificationLabel(context: Context): String =
    when {
        isGif -> context.getString(R.string.gif)
        else -> {
            when (sourceType) {
                SourceType.Screenshot, SourceType.Recording -> context.getString(
                    sourceType.labelRes
                )

                SourceType.Camera -> context.getString(
                    when (fileType) {
                        PresetFileType.Image -> R.string.photo
                        PresetFileType.Video -> R.string.video
                        else -> throw IllegalArgumentException()
                    }
                )

                SourceType.Download -> context.getString(
                    R.string.file_type_download,
                    fileType.label(context)
                )

                SourceType.OtherApp -> "/${mediaStoreFileData.parentDirName} ${fileType.label(context)}"
            }
        }
    }
