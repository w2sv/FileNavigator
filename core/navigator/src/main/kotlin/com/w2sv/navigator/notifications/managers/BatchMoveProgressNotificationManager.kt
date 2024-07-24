package com.w2sv.navigator.notifications.managers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import androidx.annotation.IntRange
import androidx.annotation.PluralsRes
import androidx.core.app.NotificationCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.core.navigator.R
import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.moving.CancelBatchMoveBroadcastReceiver
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.AppNotificationId
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.SingleInstanceNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class BatchMoveProgressNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager,
) : SingleInstanceNotificationManager<BatchMoveProgressNotificationManager.BuilderArgs>(
    appNotificationChannel = AppNotificationChannel.MoveProgress,
    notificationManager = notificationManager,
    context = context,
    appNotificationId = AppNotificationId.MoveProgress,
) {
    sealed interface BuilderArgs {
        data class MoveProgress(
            val current: Int,
            val max: Int
        ) : BuilderArgs

        data class MoveResults(
            val moveResults: List<MoveResult>,
            val destination: MoveDestination
        ) :
            BuilderArgs
    }

    override fun getBuilder(args: BuilderArgs): AppNotificationManager<BuilderArgs>.Builder =
        when (args) {
            is BuilderArgs.MoveProgress -> {
                object : Builder() {
                    override fun build(): Notification {
                        setContentTitle(
                            context.getString(
                                R.string.move_progress_notification_title,
                                args.max
                            )
                        )
                        setSilent(true)
                        setOngoing(true)
                        setProgress(args.max, args.current, false)
                        addAction(
                            NotificationCompat.Action(
                                com.w2sv.core.common.R.drawable.ic_cancel_24,
                                context.getString(R.string.cancel),
                                PendingIntent.getBroadcast(
                                    context.applicationContext,
                                    0,
                                    CancelBatchMoveBroadcastReceiver.intent(context.applicationContext),
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                                )
                            )
                        )
                        return super.build()
                    }
                }
            }

            is BuilderArgs.MoveResults -> {
                object : Builder() {
                    override fun build(): Notification {
                        setProgress(0, 0, false)  // Hides progress bar
                        setContentTitle(context.getString(R.string.move_results))
                        setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText(
                                    contentText(
                                        moveResults = args.moveResults,
                                        destination = args.destination
                                    )
                                )
                        )
                        return super.build()
                    }

                    private fun contentText(
                        moveResults: List<MoveResult>,
                        destination: MoveDestination
                    ): CharSequence {
                        val moveResultToCount = moveResults.groupingBy { it }.eachCount()
                        val resultRowPrefix = if (moveResultToCount.size >= 2) "• " else null
                        return buildSpannedString {
                            moveResultToCount[MoveResult.Success]?.let { moveSuccessCount ->
                                resultRowPrefix?.let { append(it) }
                                append(
                                    context.resources.getQuantityString(
                                        R.plurals.successfully_moved_files_to,
                                        moveSuccessCount,
                                        moveSuccessCount
                                    )
                                )
                                bold {
                                    append(
                                        destination.shortRepresentation(context)
                                    )
                                }
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
            }
        }
}

private fun Resources.getQuantityText(
    @PluralsRes id: Int,
    @IntRange(from = 0) quantity: Int,
    vararg args: Any?
): CharSequence =
    HtmlCompat.fromHtml(getQuantityString(id, quantity, *args), HtmlCompat.FROM_HTML_MODE_COMPACT)