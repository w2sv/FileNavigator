package com.w2sv.navigator.notifications.managers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.text.SpannedString
import android.util.Size
import androidx.core.app.NotificationCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.formattedFileSize
import com.w2sv.common.utils.lineBreakSuffixed
import com.w2sv.common.utils.loadBitmapFileNotFoundHandled
import com.w2sv.common.utils.removeSlashSuffix
import com.w2sv.common.utils.slashPrefixed
import com.w2sv.core.navigator.R
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.stateInWithSynchronousInitial
import com.w2sv.navigator.moving.DestinationPickerActivity
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.AppNotificationId
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.ViewFileIfPresentActivity
import com.w2sv.navigator.notifications.managers.abstrct.MultiInstanceNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.SummarizedMultiInstanceNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NewMoveFileNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager,
    navigatorConfigDataSource: NavigatorConfigDataSource,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    private val batchMoveNotificationManager: BatchMoveNotificationManager,
) : SummarizedMultiInstanceNotificationManager<NewMoveFileNotificationManager.BuilderArgs>(
    appNotificationChannel = AppNotificationChannel.NewNavigatableFile,
    notificationManager = notificationManager,
    context = context,
    appNotificationId = AppNotificationId.NewNavigatableFile
) {
    data class BuilderArgs(
        val moveFile: MoveFile,
        val quickMoveDestination: MoveDestination?,
        override val resources: NotificationResources
    ) : MultiInstanceNotificationManager.BuilderArgs

    fun buildAndPost(moveFile: MoveFile) {
        buildAndPost(
            BuilderArgs(
                moveFile = moveFile,
                quickMoveDestination = fileAndSourceTypeToLastMoveDestinationStateFlow.lastMoveDestination(
                    moveFile.fileAndSourceType
                ),
                resources = getNotificationResources(pendingIntentRequestCodeCount = 4)
            )
        )
    }

    private val fileAndSourceTypeToLastMoveDestinationStateFlow =
        FileAndSourceTypeToLastMoveDestinationStateFlow(navigatorConfigDataSource, scope)

    private val activeNotificationBuilderArgs = mutableListOf<BuilderArgs>()

    override fun buildAndPost(args: BuilderArgs) {
        super.buildAndPost(args)

        activeNotificationBuilderArgs.add(args)

        if (activeNotificationCount >= 2) {
            batchMoveNotificationManager.buildAndPost(activeNotificationBuilderArgs)
        }
    }

    override fun cancelNotificationAndFreeResources(resources: NotificationResources) {
        super.cancelNotificationAndFreeResources(resources)

        activeNotificationBuilderArgs.removeAt(activeNotificationBuilderArgs.indexOfFirst { it.resources == resources })
        batchMoveNotificationManager.cancelOrUpdate(activeNotificationBuilderArgs)
    }

    override fun getBuilder(args: BuilderArgs): Builder =
        object : Builder() {

            override fun build(): Notification {
                setContentTitle(
                    context.getString(
                        R.string.new_move_file_notification_title,
                        args.moveFile.moveNotificationLabel(context = context)
                    )
                )
                // Set file source icon
                setLargeIcon(args.moveFile.fileAndSourceType.coloredIconBitmap(context))
                setContent()
                setActionsAndIntents()

                return super.build()
            }

            private fun setContent() {
                val bigPictureStyleSet = setBigPictureStyleIfImageOrVideo()
                if (!bigPictureStyleSet) {
                    setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(getContentText())
                    )
                }
            }

            private fun setBigPictureStyleIfImageOrVideo(): Boolean {
                when (args.moveFile.fileType) {
                    FileType.Image -> context.contentResolver.loadBitmapFileNotFoundHandled(args.moveFile.mediaUri.uri)
                    FileType.Video -> {
                        try {
                            context.contentResolver.loadThumbnail(
                                args.moveFile.mediaUri.uri,
                                Size(512, 512),
                                null
                            )
                        } catch (_: IOException) {
                            null
                        }
                    }

                    else -> null
                }
                    ?.let {
                        setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(it)
                                .run {  // .apply {} strangely not working here
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        setContentDescription(
                                            getContentText()
                                        )
                                        showBigPictureWhenCollapsed(true)
                                    } else
                                        this
                                }
                        )
                        return true
                    }
                return false
            }

            private fun getContentText(): SpannedString =
                buildSpannedString {
                    append(args.moveFile.mediaStoreData.name.lineBreakSuffixed())
                    bold { append(context.getString(R.string.directory).lineBreakSuffixed()) }
                    append(
                        args.moveFile.mediaStoreData.volumeRelativeDirPath.removeSlashSuffix()
                            .slashPrefixed()
                            .lineBreakSuffixed()
                    )
                    bold { append(context.getString(R.string.size).lineBreakSuffixed()) }
                    append(
                        formattedFileSize(args.moveFile.mediaStoreData.size)
                    )
                }

            private fun setActionsAndIntents() {
                // Set actions & intents
                val requestCodeIterator = args.resources.pendingIntentRequestCodes.iterator()

                addAction(getMoveFileAction(requestCodeIterator.next()))

                // Add quickMoveAction if quickMoveDestination present.
                args.quickMoveDestination
                    ?.let { lastMoveDestination ->
                        // Don't add action if folder doesn't exist anymore, which results in getDocumentUriFileName returning null.
                        lastMoveDestination.documentFile(context)?.name?.let { directoryName ->
                            addAction(
                                getQuickMoveAction(
                                    requestCode = requestCodeIterator.next(),
                                    lastMoveDestination = lastMoveDestination,
                                    lastMoveDestinationDirectoryName = directoryName
                                )
                            )
                        }
                    }

                setContentIntent(getViewFilePendingIntent(requestCodeIterator.next()))

                setDeleteIntent(
                    getCleanupNotificationResourcesPendingIntent(
                        requestCode = requestCodeIterator.next(),
                        notificationResources = args.resources
                    )
                )
            }

            private fun getViewFilePendingIntent(requestCode: Int)
                    : PendingIntent =
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    ViewFileIfPresentActivity.makeRestartActivityIntent(
                        context = context,
                        args = ViewFileIfPresentActivity.Args(
                            mediaUri = args.moveFile.mediaUri,
                            absPath = args.moveFile.mediaStoreData.absPath,
                            mimeType = args.moveFile.fileType.simpleStorageMediaType.mimeType,
                        ),
                        notificationResources = args.resources
                    ),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

            private fun getMoveFileAction(
                requestCode: Int,
            ): NotificationCompat.Action =
                NotificationCompat.Action(
                    com.w2sv.core.common.R.drawable.ic_app_logo_24,
                    context.getString(R.string.move),
                    PendingIntent.getActivity(
                        context,
                        requestCode,
                        DestinationPickerActivity.makeRestartActivityIntent(
                            args = DestinationPickerActivity.Args.SingleFile(
                                moveFile = args.moveFile,
                                pickerStartDestination = args.quickMoveDestination?.documentUri,
                                notificationResources = args.resources,
                            ),
                            context = context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

            private fun getQuickMoveAction(
                requestCode: Int,
                lastMoveDestination: MoveDestination,
                lastMoveDestinationDirectoryName: String
            ): NotificationCompat.Action =
                NotificationCompat.Action(
                    com.w2sv.core.common.R.drawable.ic_app_logo_24,
                    context.getString(R.string.to, lastMoveDestinationDirectoryName),
                    PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        MoveBroadcastReceiver.getIntent(
                            MoveBroadcastReceiver.Args(
                                moveBundle = MoveBundle(
                                    file = args.moveFile,
                                    destination = lastMoveDestination,
                                    mode = MoveMode.Quick
                                ),
                                notificationResources = args.resources,
                            ),
                            context = context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
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

private class FileAndSourceTypeToLastMoveDestinationStateFlow(
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val scope: CoroutineScope,
    private val mutableMap: MutableMap<FileAndSourceType, StateFlow<List<MoveDestination>>> = mutableMapOf()
) : Map<FileAndSourceType, StateFlow<List<MoveDestination>>> by mutableMap {

    fun lastMoveDestination(fileAndSourceType: FileAndSourceType): MoveDestination? =
        mutableMap.getOrPut(
            key = fileAndSourceType,
            defaultValue = {
                navigatorConfigDataSource
                    .lastMoveDestination(
                        fileType = fileAndSourceType.fileType,
                        sourceType = fileAndSourceType.sourceType
                    )
                    .stateInWithSynchronousInitial(scope)
            }
        )
            .value
            .firstOrNull()
}