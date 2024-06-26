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
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.lineBreakSuffixed
import com.w2sv.common.utils.loadBitmapFileNotFoundHandled
import com.w2sv.common.utils.removeSlashSuffix
import com.w2sv.common.utils.slashPrefixed
import com.w2sv.core.navigator.R
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.stateInWithSynchronousInitial
import com.w2sv.navigator.moving.FileMoveActivity
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.MoveFile
import com.w2sv.navigator.moving.MoveMode
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.ViewFileIfPresentActivity
import com.w2sv.navigator.notifications.managers.abstrct.MultiInstanceAppNotificationManager
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
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope
) : MultiInstanceAppNotificationManager<NewMoveFileNotificationManager.BuilderArgs>(
    notificationChannel = AppNotificationChannel.NewNavigatableFile.getNotificationChannel(context),
    notificationManager = notificationManager,
    context = context,
    resourcesBaseSeed = 1,
    summaryProperties = SummaryProperties(999)
) {
    inner class BuilderArgs(
        val moveFile: MoveFile,
    ) : MultiInstanceAppNotificationManager.BuilderArgs(
        resources = getNotificationResources(
            pendingIntentRequestCodeCount = 4
        )
    )

    private val sourceToLastMoveDestinationStateFlow =
        SourceToLastMoveDestinationStateFlow(navigatorConfigDataSource, scope)

    override fun getBuilder(args: BuilderArgs): Builder =
        object : Builder() {

            override fun build(): Notification {
                setContentTitle(
                    context.getString(
                        R.string.new_move_file_notification_title,
                        args.moveFile.fileAndSourceType.moveFileLabel(
                            context = context,
                            isGif = args.moveFile.isGif,
                            sourceDirName = args.moveFile.mediaStoreFile.columnData.dirName
                        )
                    )
                )
                // Set file source icon
                setLargeIcon(args.moveFile.fileAndSourceType.coloredIconBitmap(context))
                setContent()
                setActionsAndIntents()

                return super.build()
            }

            private fun setContent() {
                val bigPictureStyleSet = setBigPictureStyleIfImage()
                if (!bigPictureStyleSet) {
                    setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(getContentText())
                    )
                }
            }

            private fun setBigPictureStyleIfImage(): Boolean {
                when (args.moveFile.fileType) {
                    FileType.Image -> context.contentResolver.loadBitmapFileNotFoundHandled(args.moveFile.mediaStoreFile.mediaUri.uri)
                    FileType.Video -> {
                        try {
                            context.contentResolver.loadThumbnail(
                                args.moveFile.mediaStoreFile.mediaUri.uri,
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
                                .run {  // .apply {} strangely not working here; possibly kotlin 2.0-related issue
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
                    append(args.moveFile.mediaStoreFile.columnData.name.lineBreakSuffixed())
                    bold { append(context.getString(R.string.found_at).lineBreakSuffixed()) }
                    append(
                        args.moveFile.mediaStoreFile.columnData.volumeRelativeDirPath.removeSlashSuffix()
                            .slashPrefixed()
                    )
                }

            private fun setActionsAndIntents() {
                // Set actions & intents
                val requestCodeIterator = args.resources.pendingIntentRequestCodes.iterator()

                addAction(getMoveFileAction(requestCodeIterator.next()))

                // Add quickMoveAction if lastMoveDestination present.
                sourceToLastMoveDestinationStateFlow.lastMoveDestination(args.moveFile.fileAndSourceType)
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
                            mediaUri = args.moveFile.mediaStoreFile.mediaUri,
                            absPath = args.moveFile.mediaStoreFile.columnData.absPath,
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
                    R.drawable.ic_app_logo_24,
                    context.getString(R.string.move),
                    PendingIntent.getActivity(
                        context,
                        requestCode,
                        FileMoveActivity.makeRestartActivityIntent(
                            args.moveFile,
                            args.resources,
                            context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

            private fun getQuickMoveAction(
                requestCode: Int,
                lastMoveDestination: DocumentUri,
                lastMoveDestinationDirectoryName: String
            ): NotificationCompat.Action =
                NotificationCompat.Action(
                    R.drawable.ic_app_logo_24,
                    context.getString(R.string.to, lastMoveDestinationDirectoryName),
                    PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        MoveBroadcastReceiver.getIntent(
                            moveFile = args.moveFile.copy(moveMode = MoveMode.Quick(destination = lastMoveDestination)),
                            notificationResources = args.resources,
                            context = context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
        }

    override fun buildSummaryNotification(): Notification =
        Builder()
            .setContentTitle(
                context.getString(
                    if (nActiveNotifications >= 2) R.string.navigatable_files else R.string.navigatable_file,
                    nActiveNotifications
                )
            )
            .setOnlyAlertOnce(true)
            .setGroupSummary(true)
            .build()
}

private class SourceToLastMoveDestinationStateFlow(
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val scope: CoroutineScope,
    private val mutableMap: MutableMap<FileAndSourceType, StateFlow<List<DocumentUri>>> = mutableMapOf()
) : Map<FileAndSourceType, StateFlow<List<DocumentUri>>> by mutableMap {

    fun lastMoveDestination(fileAndSourceType: FileAndSourceType): DocumentUri? =
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