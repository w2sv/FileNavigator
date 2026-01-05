package com.w2sv.navigator.notifications.controller

import android.app.PendingIntent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.res.isNightModeActiveCompat
import com.w2sv.core.common.R
import com.w2sv.navigator.domain.NavigatorIntents
import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent
import com.w2sv.navigator.notifications.AppNotification
import com.w2sv.navigator.notifications.api.NotificationEnvironment
import com.w2sv.navigator.notifications.api.SingleNotificationController
import com.w2sv.navigator.notifications.helper.drawableBitmap
import javax.inject.Inject

internal typealias BatchMoveNotificationControllerArgs = Map<CancelNotificationEvent, MoveFileNotificationController.Args>

internal class BatchMoveNotificationController @Inject constructor(
    environment: NotificationEnvironment,
    private val navigatorIntents: NavigatorIntents
) : SingleNotificationController<BatchMoveNotificationControllerArgs>(
    environment = environment,
    appNotification = AppNotification.BatchMoveFiles
) {

    private enum class PendingIntentRequestCode {
        MoveAction,
        QuickMoveAction
    }

    override fun NotificationCompat.Builder.configure(args: BatchMoveNotificationControllerArgs, id: Int) {
        setGroup(channel.id)
        setContentTitle(
            context.getString(
                R.string.move_files,
                args.size
            )
        )
        setLargeIcon(
            context.drawableBitmap(
                drawable = R.drawable.ic_files_24,
                tint = if (context.resources.configuration.isNightModeActiveCompat) null else Color.BLACK
            )
        )
        setSilent(true)
        addAction(moveFilesAction(args.moveFileNotificationData()))
        addQuickMoveActions(args)
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

    // TODO: refactor
    private fun NotificationCompat.Builder.addQuickMoveActions(args: BatchMoveNotificationControllerArgs) {
        val occurrenceOrderedQuickMoveDestinations =
            args.values.flatMap { it.quickMoveDestinations }
                .groupingBy { it }
                .eachCount().entries.sortedByDescending { it.value }
                .map { it.key }

        var addedQuickMoveActions = 0
        occurrenceOrderedQuickMoveDestinations.forEach { moveDestination ->
            // Don't add action if folder doesn't exist anymore, which results in getDocumentUriFileName returning null.
            moveDestination.documentFile(context).name?.let { directoryName ->
                addAction(
                    quickMoveAction(
                        destinationDirectoryName = directoryName,
                        requestCode = PendingIntentRequestCode.QuickMoveAction.ordinal + addedQuickMoveActions,
                        batchMoveBundles = args.quickMoveBundles(moveDestination)
                    )
                )
                addedQuickMoveActions += 1
                if (addedQuickMoveActions == 2) {
                    return
                }
            }
        }
    }

    private fun quickMoveAction(destinationDirectoryName: String, requestCode: Int, batchMoveBundles: List<MoveOperation.QuickMove>) =
        NotificationCompat.Action(
            R.drawable.ic_app_logo_24,
            context.getString(R.string.to, destinationDirectoryName),
            PendingIntent.getBroadcast(
                context,
                requestCode,
                navigatorIntents.startBatchMove(batchMoveBundles),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

    fun cancelOrUpdate(args: BatchMoveNotificationControllerArgs) {
        if (args.size <= 1) {
            cancel()
        } else {
            post(args)
        }
    }
}

private fun BatchMoveNotificationControllerArgs.moveFileNotificationData(): List<MoveFileNotificationData> =
    map { (cancelEvent, args) ->
        MoveFileNotificationData(
            moveFile = args.moveFile,
            cancelNotificationEvent = cancelEvent
        )
    }

private fun BatchMoveNotificationControllerArgs.quickMoveBundles(destination: MoveDestination.Directory): List<MoveOperation.QuickMove> =
    map { (cancelEvent, args) ->
        MoveOperation.QuickMove(
            file = args.moveFile,
            destinationSelectionManner = DestinationSelectionManner.Quick(cancelEvent),
            destination = destination,
            isPartOfBatch = true
        )
    }
