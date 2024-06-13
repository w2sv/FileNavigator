package com.w2sv.navigator.notifications.managers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.SpannedString
import android.util.Size
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.androidutils.coroutines.stateInWithSynchronousInitial
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.lineBreakSuffixed
import com.w2sv.common.utils.loadBitmap
import com.w2sv.common.utils.removeSlashSuffix
import com.w2sv.common.utils.slashPrefixed
import com.w2sv.core.navigator.R
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.navigator.moving.FileMoveActivity
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.MoveFile
import com.w2sv.navigator.moving.MoveMode
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.NotificationResourcesCleanupBroadcastReceiver
import com.w2sv.navigator.notifications.ViewFileIfPresentActivity
import com.w2sv.navigator.notifications.managers.abstrct.MultiInstanceAppNotificationManager
import dagger.hilt.android.AndroidEntryPoint
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
                    "${context.getString(R.string.new_)} ${getMoveFileTitle()}"
                )
                // Set file source icon
                setLargeIcon(
                    AppCompatResources.getDrawable(context, args.moveFile.fileAndSourceType.iconRes)
                        ?.apply { setTint(args.moveFile.fileType.colorInt) }
                        ?.toBitmap()
                )
                setContent()
                setActionsAndIntents()

                return super.build()
            }

            private fun getMoveFileTitle(): String =
                when (val fileType = args.moveFile.fileType) {
                    is FileType.Media -> {
                        if (isGif(args.moveFile)) {
                            context.getString(R.string.gif)
                        } else {
                            when (val sourceType =
                                args.moveFile.sourceType) {
                                SourceType.Recording, SourceType.Screenshot -> context.getString(
                                    sourceType.labelRes
                                )

                                SourceType.Camera -> context.getString(
                                    when (args.moveFile.fileType) {
                                        FileType.Image -> com.w2sv.core.domain.R.string.photo
                                        FileType.Video -> com.w2sv.core.domain.R.string.video
                                        else -> throw Error()
                                    }
                                )

                                SourceType.Download -> "${context.getString(fileType.labelRes)} ${
                                    context.getString(R.string.download)
                                }"

                                SourceType.OtherApp -> "${args.moveFile.mediaStoreFile.columnData.dirName} ${
                                    context.getString(
                                        fileType.labelRes
                                    )
                                }"
                            }
                        }
                    }

                    is FileType.NonMedia -> {
                        context.getString(args.moveFile.fileType.labelRes)
                    }
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
                    FileType.Image -> context.contentResolver.loadBitmap(args.moveFile.mediaStoreFile.uri)
                    FileType.Video -> {
                        try {
                            context.contentResolver.loadThumbnail(
                                args.moveFile.mediaStoreFile.uri,
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
                        mediaUri = args.moveFile.mediaStoreFile.uri,
                        absPath = args.moveFile.mediaStoreFile.columnData.absPath,
                        mimeType = args.moveFile.fileType.simpleStorageMediaType.mimeType,
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

            private fun getCleanupNotificationResourcesPendingIntent(
                requestCode: Int
            ): PendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    ResourcesCleanupBroadcastReceiver.getIntent(
                        context,
                        args.resources
                    ),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
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

    @AndroidEntryPoint
    class ResourcesCleanupBroadcastReceiver : NotificationResourcesCleanupBroadcastReceiver() {

        @Inject
        lateinit var newMoveFileNotificationManager: NewMoveFileNotificationManager

        override val multiInstanceAppNotificationManager: MultiInstanceAppNotificationManager<*>
            get() = newMoveFileNotificationManager

        companion object {
            fun getIntent(
                context: Context,
                notificationResources: NotificationResources
            ): Intent =
                getIntent<ResourcesCleanupBroadcastReceiver>(
                    context,
                    notificationResources
                )

            fun startFromResourcesComprisingIntent(
                context: Context,
                intent: Intent
            ) {
                startFromResourcesComprisingIntent<ResourcesCleanupBroadcastReceiver>(
                    context,
                    intent
                )
            }
        }
    }
}

private fun isGif(moveFile: MoveFile): Boolean =
    moveFile.fileType is FileType.Image && moveFile.mediaStoreFile.columnData.fileExtension.lowercase() == "gif"

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