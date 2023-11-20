package com.w2sv.navigator.notifications.managers.newmovefile

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannedString
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.common.utils.getDocumentUriFileName
import com.w2sv.domain.model.FileType
import com.w2sv.navigator.R
import com.w2sv.navigator.model.MoveFile
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.getNotificationChannel
import com.w2sv.navigator.notifications.managers.AppNotificationsManager
import com.w2sv.navigator.notifications.managers.abstrct.MultiInstanceAppNotificationManager
import com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.FileMoveActivity
import com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers.NotificationResourcesCleanupBroadcastReceiver
import com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers.QuickMoveBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i
import javax.inject.Inject

class NewMoveFileNotificationManager(
    context: Context,
    notificationManager: NotificationManager
) : MultiInstanceAppNotificationManager<NewMoveFileNotificationManager.BuilderArgs>(
    notificationChannel = AppNotificationChannel.NewFile.getNotificationChannel(context),
    notificationManager = notificationManager,
    context = context,
    resourcesBaseSeed = 1,
    summaryId = 999,
) {
    inner class BuilderArgs(
        val moveFile: MoveFile,
        val getLastMoveDestination: (FileType.Source) -> Uri?,
    ) : MultiInstanceAppNotificationManager.BuilderArgs(getNotificationResources(4))

    override fun getBuilder(args: BuilderArgs): Builder =
        object : Builder() {

            override fun build(): Notification {
                setContentTitle(
                    "${context.getString(R.string.new_)} ${getMoveFileTitle()}"
                )
                // Set icons
                setLargeIcon(
                    AppCompatResources.getDrawable(context, args.moveFile.source.getIconRes())
                        ?.apply { setTint(args.moveFile.source.fileType.colorInt) }
                        ?.toBitmap()
                )
                // Set content
                setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(getContentText())
                )

                // Set actions & intents
                val requestCodeIterator = args.resources.actionRequestCodes.iterator()

                addAction(getMoveFileAction(requestCodeIterator.next()))

                // Add quickMoveAction if lastMoveDestination present.
                args.getLastMoveDestination(args.moveFile.source)?.let { lastMoveDestination ->
                    // Don't add action if folder doesn't exist anymore, which results in getDocumentUriFileName returning null.
                    getDocumentUriFileName(
                        documentUri = lastMoveDestination,
                        context = context
                    )?.let { fileName ->
                        addAction(
                            getQuickMoveAction(
                                requestCode = requestCodeIterator.next(),
                                lastMoveDestination = lastMoveDestination,
                                lastMoveDestinationFileName = fileName
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

                return super.build()
            }

            private fun getMoveFileTitle(): String =
                when (val fileType = args.moveFile.source.fileType) {
                    is FileType.Media -> {
                        when (val sourceKind = args.moveFile.mediaStoreFile.columnData.getSourceKind()) {
                            FileType.Source.Kind.Recording, FileType.Source.Kind.Screenshot -> context.getString(sourceKind.labelRes)

                            FileType.Source.Kind.Camera -> context.getString(
                                when (args.moveFile.source.fileType) {
                                    FileType.Image -> com.w2sv.domain.R.string.photo
                                    FileType.Video -> com.w2sv.domain.R.string.video
                                    else -> throw Error()
                                }
                            )

                            FileType.Source.Kind.Download -> "${
                                context.getString(
                                    R.string.downloaded
                                )
                            } ${context.getString(fileType.titleRes)}"

                            FileType.Source.Kind.OtherApp -> "/${args.moveFile.mediaStoreFile.columnData.dirName} ${
                                context.getString(
                                    fileType.titleRes
                                )
                            }"
                        }
                    }

                    is FileType.NonMedia -> {
                        context.getString(args.moveFile.source.fileType.titleRes)
                    }
                }

            private fun getContentText(): SpannedString =
                buildSpannedString {
                    bold { append(args.moveFile.mediaStoreFile.columnData.name) }
                    append(" ${context.getString(R.string.found_at)} ")
                    bold {
                        append(
                            "/${
                                args.moveFile.mediaStoreFile.columnData.volumeRelativeDirPath.removeSuffix(
                                    "/"
                                )
                            }"
                        )
                    }
                }

            private fun getViewFilePendingIntent(requestCode: Int)
                    : PendingIntent =
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    Intent()
                        .setAction(Intent.ACTION_VIEW)
                        .setDataAndType(
                            args.moveFile.mediaStoreFile.uri,
                            args.moveFile.source.fileType.simpleStorageMediaType.mimeType
                        ),
                    PendingIntent.FLAG_IMMUTABLE
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
                lastMoveDestination: Uri,
                lastMoveDestinationFileName: String
            ): NotificationCompat.Action =
                NotificationCompat.Action(
                    R.drawable.ic_app_logo_24,
                    "to /$lastMoveDestinationFileName",
                    PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        QuickMoveBroadcastReceiver.getIntent(
                            args.moveFile,
                            args.resources,
                            lastMoveDestination.also {
                                i { "MoveDestination Extra: $it" }
                            },
                            context
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
            .setGroupSummary(true)
            .setContentTitle(
                context.getString(
                    R.string.new_navigatable_files,
                    nActiveNotifications
                )
            )
            .build()

    @AndroidEntryPoint
    class ResourcesCleanupBroadcastReceiver : NotificationResourcesCleanupBroadcastReceiver() {

        @Inject
        lateinit var appNotificationsManager: AppNotificationsManager

        override val multiInstanceAppNotificationManager: MultiInstanceAppNotificationManager<*>
            get() = appNotificationsManager.newMoveFileNotificationManager

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