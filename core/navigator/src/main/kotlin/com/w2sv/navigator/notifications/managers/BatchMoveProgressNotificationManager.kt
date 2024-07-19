package com.w2sv.navigator.notifications.managers

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.core.navigator.R
import com.w2sv.navigator.moving.model.MoveDestination
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.SingleInstanceAppNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class BatchMoveProgressNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager,
) : SingleInstanceAppNotificationManager<BatchMoveProgressNotificationManager.BuilderArgs>(
    appNotificationChannel = AppNotificationChannel.MoveProgress,
    notificationManager = notificationManager,
    context = context,
    notificationId = 97892
) {
    sealed interface BuilderArgs : AppNotificationManager.BuilderArgs {
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

    override fun getBuilder(args: BuilderArgs): AppNotificationManager<BuilderArgs>.Builder {
        return object : Builder() {
            override fun build(): Notification {
                setSilent(true)

                when (args) {
                    is BuilderArgs.MoveProgress -> {
                        setContentTitle(
                            context.getString(
                                R.string.move_progress_notification_title,
                                args.max
                            )
                        )
                        setProgress(args.max, args.current, false)
                    }

                    is BuilderArgs.MoveResults -> {
                        setProgress(0, 0, false)  // Hides progress bar
                        setContentTitle(context.getString(R.string.move_results))
                        setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText(
                                    resultsContentText(
                                        moveResults = args.moveResults,
                                        destination = args.destination
                                    )
                                )
                        )
                    }
                }

                return super.build()
            }

            private fun resultsContentText(
                moveResults: List<MoveResult>,
                destination: MoveDestination
            ): CharSequence {
                val moveResultTypeToCount = moveResults.groupingBy { it::class.java }.eachCount()
                return buildSpannedString {
                    moveResultTypeToCount[MoveResult.Success::class.java]?.let {
                        append(context.getString(R.string.successfully_moved_files_to, it))
                        bold {
                            append(
                                destination.shortRepresentation(context)
                            )
                        }
                    }
                    if (moveResultTypeToCount.keys.any { it != MoveResult.Success::class.java }) {
                        moveResultTypeToCount.keys.filter { it != MoveResult.Success::class.java }
                            .forEachIndexed { index, moveFailureType ->
                                if (index != 0 || moveResultTypeToCount.containsKey(MoveResult.Success::class.java)) {
                                    append("\n")
                                }
                                append(
                                    context.resources.getQuantityString(
                                        R.plurals.couldn_t_move_files_due_to,
                                        moveResultTypeToCount.getValue(moveFailureType),
                                        moveResultTypeToCount.getValue(moveFailureType),
                                        when (moveFailureType) {
                                            MoveResult.Failure.InternalError::class.java -> "internal error"
                                            MoveResult.Failure.MoveFileNotFound::class.java -> "file not found"
                                            MoveResult.Failure.FileAlreadyAtDestination::class.java -> "file already at destination"
                                            MoveResult.Failure.NotEnoughSpaceOnDestination::class.java -> "not enough space on destination"
                                            else -> ""
                                        }
                                    )
                                )
                            }
                    }
                }
            }
        }
    }
}