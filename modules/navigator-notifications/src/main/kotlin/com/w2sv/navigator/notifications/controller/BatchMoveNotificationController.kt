package com.w2sv.navigator.notifications.controller

import android.app.PendingIntent
import android.graphics.Color
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.res.isNightModeActiveCompat
import com.w2sv.modules.common.R
import com.w2sv.navigator.domain.NavigatorIntents
import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.notifications.AppNotification
import com.w2sv.navigator.notifications.api.controller.SingleNotificationController
import com.w2sv.navigator.notifications.api.env.NotificationEnvironment
import com.w2sv.navigator.notifications.helper.drawableBitmap
import javax.inject.Inject

internal typealias BatchMoveNotificationArgs = Map<Int, NavigateFileNotificationController.Args>

internal class BatchMoveNotificationController @Inject constructor(
    environment: NotificationEnvironment,
    private val navigatorIntents: NavigatorIntents
) : SingleNotificationController<BatchMoveNotificationArgs>(
    environment = environment,
    appNotification = AppNotification.BatchMoveFiles
) {

    private enum class PendingIntentRequestCode {
        MoveAction,
        QuickMoveAction
    }

    override fun NotificationCompat.Builder.configure(args: BatchMoveNotificationArgs, id: Int) {
        setGroup(channel.id)
        setSortKey("0") // Make notification appear first in the group
        setSilent(true) // Notification will be posted in addition to each 'navigate file' notification; we don't want a double alert

        setContentTitle(context.resources.getQuantityString(R.plurals.move_files, args.size, args.size))
        setLargeIcon(
            context.drawableBitmap(
                drawable = R.drawable.ic_files_24,
                tint = if (context.resources.configuration.isNightModeActiveCompat) null else Color.BLACK
            )
        )
        addAction(moveFilesAction(args.moveFileNotificationData()))
        quickMoveActions(args).forEach(::addAction)
    }

    private fun moveFilesAction(moveFileNotificationData: List<MoveFileNotificationData>) =
        NotificationCompat.Action(
            R.drawable.ic_app_logo_24,
            context.getString(R.string.move),
            PendingIntent.getActivity(
                context,
                PendingIntentRequestCode.MoveAction.ordinal,
                navigatorIntents.pickBatchMoveDestination(args = moveFileNotificationData, startDestination = null),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

    private fun quickMoveActions(args: BatchMoveNotificationArgs): List<NotificationCompat.Action> =
        buildList {
            args.frequencyOrderedQuickMoveDestinations().forEach { moveDestination ->
                // Don't add action if folder doesn't exist anymore, which results in getDocumentUriFileName returning null.
                moveDestination.documentFile(context).name?.let { directoryName ->
                    add(
                        quickMoveAction(
                            destinationDirectoryName = directoryName,
                            requestCode = PendingIntentRequestCode.QuickMoveAction.ordinal + size,
                            batchMoveBundles = args.quickMoveBundles(moveDestination)
                        )
                    )
                }
                if (size == 2) {
                    return@forEach
                }
            }
        }

    private fun quickMoveAction(destinationDirectoryName: String, requestCode: Int, batchMoveBundles: List<MoveOperation.QuickMove>) =
        NotificationCompat.Action(
            R.drawable.ic_app_logo_24,
            context.getString(R.string.to, destinationDirectoryName),
            PendingIntent.getService(
                context,
                requestCode,
                navigatorIntents.startBatchMove(batchMoveBundles),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
}

@VisibleForTesting
internal fun BatchMoveNotificationArgs.frequencyOrderedQuickMoveDestinations(): List<MoveDestination.Directory> =
    values
        .flatMap { it.quickMoveDestinations }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { (_, count) -> count }
        .map { (destination, _) -> destination }

private fun BatchMoveNotificationArgs.moveFileNotificationData(): List<MoveFileNotificationData> =
    map { (id, args) ->
        MoveFileNotificationData(
            navigatableFile = args.navigatableFile,
            cancelNotificationEvent = NotificationEvent.CancelNavigateFile(id)
        )
    }

private fun BatchMoveNotificationArgs.quickMoveBundles(destination: MoveDestination.Directory): List<MoveOperation.QuickMove> =
    map { (id, args) ->
        MoveOperation.QuickMove(
            file = args.navigatableFile,
            destinationSelectionManner = DestinationSelectionManner.Quick(NotificationEvent.CancelNavigateFile(id)),
            destination = destination,
            isPartOfBatch = true
        )
    }
