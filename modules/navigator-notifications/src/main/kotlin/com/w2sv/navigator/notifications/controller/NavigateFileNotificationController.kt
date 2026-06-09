package com.w2sv.navigator.notifications.controller

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.text.SpannedString
import android.util.Size
import androidx.core.app.NotificationCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.core.di.ApplicationDefaultScope
import com.w2sv.core.logging.log
import com.w2sv.core.util.formattedFileSize
import com.w2sv.core.util.lineBreakSuffixed
import com.w2sv.core.util.removeSlashSuffix
import com.w2sv.core.util.slashPrefixed
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.modules.resources.R
import com.w2sv.navigator.domain.NavigatorIntents
import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.NavigatableFile
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.notifications.AppNotification
import com.w2sv.navigator.notifications.NotificationEventReceiver
import com.w2sv.navigator.notifications.api.controller.MultiNotificationController
import com.w2sv.navigator.notifications.api.env.NotificationEnvironment
import com.w2sv.navigator.notifications.api.setBigTextStyle
import com.w2sv.navigator.notifications.helper.GetQuickMoveDestinations
import com.w2sv.navigator.notifications.helper.iconBitmap
import com.w2sv.storage.util.loadBitmapWithFileNotFoundHandling
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import slimber.log.i

internal class NavigateFileNotificationController @Inject constructor(
    environment: NotificationEnvironment,
    private val getQuickMoveDestinations: GetQuickMoveDestinations,
    private val navigatorIntents: NavigatorIntents,
    @ApplicationDefaultScope private val scope: CoroutineScope
) : MultiNotificationController<NavigateFileNotificationController.Args>(
    environment = environment,
    appNotification = AppNotification.NavigateFile,
    summaryNotification = { context, activeNotifications ->
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
    data class Args(val navigatableFile: NavigatableFile, val quickMoveDestinations: List<MoveDestination.Directory>)

    internal sealed interface UpdateEvent {
        data class Added(val id: Int, val args: Args) : UpdateEvent
        data class Cancelled(val id: Int) : UpdateEvent
    }

    val updates: SharedFlow<UpdateEvent>
        field = MutableSharedFlow()

    fun post(navigatableFile: NavigatableFile) {
        scope.launch {
            val args = Args(
                navigatableFile = navigatableFile,
                quickMoveDestinations = getQuickMoveDestinations(navigatableFile.fileAndSourceType)
                    .log { "Retrieved quickMoveDestination: $it" }
            )
            val id = post(args)
            updates.emit(UpdateEvent.Added(id, args))
        }
    }

    override fun cancel(id: Int) {
        super.cancel(id)
        scope.launch { updates.emit(UpdateEvent.Cancelled(id)) }
    }

    // =====================
    // Notification building
    // =====================

    override fun NotificationCompat.Builder.configure(args: Args, id: Int) {
        setContentTitle(args.navigatableFile.notificationTitle(context))
        setLargeIcon(args.navigatableFile.largeNotificationIcon(context))
        setBigTextStyle(args.navigatableFile.notificationContentText(context))
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
        val notificationData = MoveFileNotificationData(args.navigatableFile, cancelEvent(id))
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

    private fun cancelEvent(id: Int): NotificationEvent.CancelNavigateFile =
        NotificationEvent.CancelNavigateFile(id)

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
                    file = args.navigatableFile,
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
                        file = args.navigatableFile,
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

private fun NavigatableFile.largeNotificationIcon(context: Context): Bitmap? =
    when (fileType.presetTypeOrNull) {
        PresetFileType.Image -> context.contentResolver.loadBitmapWithFileNotFoundHandling(mediaUri.uri)
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

private fun NavigatableFile.notificationContentText(context: Context): SpannedString =
    buildSpannedString {
        append(mediaStoreEntry.fileName.lineBreakSuffixed())
        bold { append(context.getString(R.string.location).lineBreakSuffixed()) }
        append(
            mediaStoreEntry.relativePath
                .removeSlashSuffix()
                .slashPrefixed()
                .lineBreakSuffixed()
        )
        bold { append(context.getString(R.string.size).lineBreakSuffixed()) }
        append(formattedFileSize(mediaStoreEntry.size))
    }

private fun NavigatableFile.notificationTitle(context: Context): String =
    context.getString(
        R.string.navigate_file_notification_title,
        label(context = context)
    )
