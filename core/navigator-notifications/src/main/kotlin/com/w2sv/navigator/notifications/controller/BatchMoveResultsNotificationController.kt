package com.w2sv.navigator.notifications.controller

import androidx.core.app.NotificationCompat
import androidx.core.text.buildSpannedString
import com.w2sv.androidutils.res.getQuantityText
import com.w2sv.core.common.R
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveResult
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.notifications.AppNotification
import com.w2sv.navigator.notifications.api.NotificationEnvironment
import com.w2sv.navigator.notifications.api.SingleNotificationController
import com.w2sv.navigator.notifications.api.setBigTextStyle
import javax.inject.Inject

internal class BatchMoveResultsNotificationController @Inject constructor(environment: NotificationEnvironment) :
    SingleNotificationController<NotificationEvent.BatchMoveResults>(
        environment = environment,
        appNotification = AppNotification.BatchMoveProgress
    ) {

    override fun NotificationCompat.Builder.configure(args: NotificationEvent.BatchMoveResults, id: Int) {
        // TODO shouldn't be the classes responsibility
        setProgress(0, 0, false) // Hides progress bar

        val moveResultToCount = args.moveResults.groupingBy { it }.eachCount()
        val contentText = contentText(
            moveResultToCount = moveResultToCount,
            destination = args.destination
        )

        if (moveResultToCount.size == 1) {
            setContentTitle(contentText)
        } else {
            setContentTitle(context.getString(R.string.move_results))
            setBigTextStyle(
                contentText(
                    moveResultToCount = moveResultToCount,
                    destination = args.destination
                )
            )
        }
    }

    private fun contentText(moveResultToCount: Map<MoveResult, Int>, destination: MoveDestination.Directory): CharSequence {
        val resultRowPrefix = if (moveResultToCount.size >= 2) "• " else null
        return buildSpannedString {
            moveResultToCount[MoveResult.Success]?.let { moveSuccessCount ->
                resultRowPrefix?.let { append(it) }
                append(
                    context.resources.getQuantityText(
                        R.plurals.moved_files_to,
                        moveSuccessCount,
                        moveSuccessCount,
                        destination.uiRepresentation(context)
                    )
                )
            }
            moveResultToCount.keys
                .filter { it != MoveResult.Success }
                .forEachIndexed { index, moveFailureType ->
                    if (index != 0 || moveResultToCount.containsKey(MoveResult.Success)) {
                        append("\n")
                    }
                    resultRowPrefix?.let { append(it) }
                    append(
                        context.resources.getQuantityText(
                            R.plurals.couldn_t_move_files_due_to,
                            moveResultToCount.getValue(moveFailureType),
                            moveResultToCount.getValue(moveFailureType),
                            moveFailureType.javaClass.simpleName
                        )
                    )
                }
        }
    }
}
