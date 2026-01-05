package com.w2sv.navigator.notifications.controller

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.text.SpannedString
import android.util.Size
import androidx.core.app.NotificationCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.common.logging.log
import com.w2sv.common.util.formattedFileSize
import com.w2sv.common.util.lineBreakSuffixed
import com.w2sv.common.util.loadBitmapWithFileNotFoundHandling
import com.w2sv.common.util.removeSlashSuffix
import com.w2sv.common.util.slashPrefixed
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.navigator.domain.NavigatorIntents
import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveFile
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.notifications.AppNotification
import com.w2sv.navigator.notifications.NotificationEventReceiver
import com.w2sv.navigator.notifications.api.MultiNotificationController
import com.w2sv.navigator.notifications.api.NotificationEnvironment
import com.w2sv.navigator.notifications.api.setBigTextStyle
import com.w2sv.navigator.notifications.helper.GetQuickMoveDestination
import com.w2sv.navigator.notifications.helper.iconBitmap
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.firstOrNull
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import slimber.log.i

// TODO make stateless, store state in event handler & unsingleton
@Singleton
internal class MoveFileNotificationController @Inject constructor(
    environment: NotificationEnvironment,
    private val getQuickMoveDestination: GetQuickMoveDestination,
    private val navigatorIntents: NavigatorIntents
) : MultiNotificationController<MoveFileNotificationController.Args>(
    environment = environment,
    appNotification = AppNotification.NewNavigatableFile,
    configureSummaryNotification = { context, activeNotifications ->
        this
            .setContentTitle(
                context.resources.getQuantityString(
                    R.plurals.navigatable_file,
                    activeNotifications,
                    activeNotifications
                )
            )
            .setSilent(true)
    }
) {
    data class Args(val moveFile: MoveFile, val quickMoveDestinations: List<MoveDestination.Directory>)

    val idToArgs: Map<Int, Args>
        field = mutableMapOf<Int, Args>()

    fun post(moveFile: MoveFile) {
        val args = Args(
            moveFile = moveFile,
            quickMoveDestinations = getQuickMoveDestination(moveFile.fileAndSourceType)
                .log { "Retrieved quickMoveDestination: $it" }
        )
        val id = post(args)
        idToArgs[id] = args
    }

    override fun cancel(id: Int) {
        super.cancel(id)
        idToArgs.remove(id)
    }

    // =====================
    // Notification building
    // =====================

    override fun NotificationCompat.Builder.configure(args: Args, id: Int) {
        setContentTitle(args.moveFile.notificationTitle(context))
        setLargeIcon(args.moveFile.largeNotificationIcon(context))
        setBigTextStyle(args.moveFile.notificationContentText(context))
        setActionsAndIntents(args = args, id = id)
    }

    private fun NotificationCompat.Builder.setActionsAndIntents(args: Args, id: Int) {
        // Set actions & intents
        val requestCodeIterator = (id..id + 5).iterator()

        addAction(
            getMoveFileAction(
                requestCode = requestCodeIterator.next(),
                args = args,
                id = id
            )
        )

        // Add quickMoveAction if quickMoveDestination present.
        args.quickMoveDestinations.firstOrNull()?.let { quickMoveDestination ->
            i { "quickMoveDestination=$quickMoveDestination" }

            // TODO: checking whether destination exists might be possible by querying the abs path through media uri and converting to a java.io.File
            addAction(
                quickMoveAction(
                    requestCode = requestCodeIterator.next(),
                    destination = quickMoveDestination,
                    directoryName = quickMoveDestination.fileName(context),
                    args = args,
                    id = id
                )
            )
        }
        val notificationData = MoveFileNotificationData(args.moveFile, cancelEvent(id))
        addAction(
            deleteFileAction(
                requestCode = requestCodeIterator.next(),
                data = notificationData
            )
        )
        setContentIntent(
            viewFilePendingIntent(
                requestCode = requestCodeIterator.next(),
                data = notificationData
            )
        )

        setDeleteIntent(
            NotificationEventReceiver.pendingIntent(
                context = context,
                requestCode = requestCodeIterator.next(),
                event = cancelEvent(id)
            )
        )
    }

    // =====================
    // PendingIntents / Actions
    // =====================

    private fun cancelEvent(id: Int): NotificationEvent.CancelMoveFile =
        NotificationEvent.CancelMoveFile(id)

    private fun viewFilePendingIntent(requestCode: Int, data: MoveFileNotificationData): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            navigatorIntents.viewFile(data),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun getMoveFileAction(requestCode: Int, args: Args, id: Int) =
        NotificationCompat.Action(
            R.drawable.ic_app_logo_24,
            context.getString(R.string.move),
            PendingIntent.getActivity(
                context,
                requestCode,
                navigatorIntents.pickFileDestination(
                    file = args.moveFile,
                    startDestination = args.quickMoveDestinations.firstOrNull()?.documentUri,
                    cancelNotification = cancelEvent(id)
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

    private fun quickMoveAction(
        requestCode: Int,
        destination: MoveDestination.Directory,
        directoryName: String,
        args: Args,
        id: Int
    ) =
        NotificationCompat.Action(
            R.drawable.ic_app_logo_24,
            context.getString(R.string.to, directoryName),
            PendingIntent.getActivity(
                context,
                requestCode,
                navigatorIntents.quickMoveWithPermissionCheck(
                    MoveOperation.QuickMove(
                        file = args.moveFile,
                        destination = destination,
                        destinationSelectionManner = DestinationSelectionManner.Quick(cancelEvent(id)),
                        isPartOfBatch = false
                    )
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

    private fun deleteFileAction(requestCode: Int, data: MoveFileNotificationData) =
        NotificationCompat.Action(
            R.drawable.ic_delete_24,
            context.getString(R.string.delete),
            PendingIntent.getActivity(
                context,
                requestCode,
                navigatorIntents.deleteFile(data),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
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
        append(mediaStoreData.name.lineBreakSuffixed())
        bold { append(context.getString(R.string.directory).lineBreakSuffixed()) }
        append(
            mediaStoreData.volumeRelativeDirPath
                .removeSlashSuffix()
                .slashPrefixed()
                .lineBreakSuffixed()
        )
        bold { append(context.getString(R.string.size).lineBreakSuffixed()) }
        append(formattedFileSize(mediaStoreData.size))
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

                SourceType.OtherApp -> "/${mediaStoreData.parentDirName} ${fileType.label(context)}"
            }
        }
    }
