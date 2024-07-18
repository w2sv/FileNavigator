package com.w2sv.navigator.notifications.managers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.w2sv.core.navigator.R
import com.w2sv.navigator.moving.DestinationPickerActivity
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.SingleInstanceAppNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class BatchMoveNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager,
) : SingleInstanceAppNotificationManager<BatchMoveNotificationManager.BuilderArgs>(
    appNotificationChannel = AppNotificationChannel.BatchMoveFiles,
    notificationManager = notificationManager,
    context = context,
    notificationId = 9867234
) {
    data class BuilderArgs(
        val moveFiles: List<MoveFile>,
        val notificationResources: List<NotificationResources>
    ) : AppNotificationManager.BuilderArgs

    fun buildAndPost(newMoveFileNotificationBuilderArgs: List<NewMoveFileNotificationManager.BuilderArgs>) {
        buildAndPostNotification(
            BuilderArgs(
                moveFiles = newMoveFileNotificationBuilderArgs.map { it.moveFile },
                notificationResources = newMoveFileNotificationBuilderArgs.map { it.resources },
            )
        )
    }

    override fun getBuilder(args: BuilderArgs): Builder =
        object : Builder() {
            override fun build(): Notification {
                setGroup(notificationChannel.id)
                setContentTitle(context.getString(R.string.move_files, args.moveFiles.size))
                setSilent(true)
                addAction(getMoveFilesAction())
                return super.build()
            }

            private fun getMoveFilesAction(
            ): NotificationCompat.Action =
                NotificationCompat.Action(
                    com.w2sv.core.common.R.drawable.ic_app_logo_24,
                    context.getString(R.string.move),
                    PendingIntent.getActivity(
                        context,
                        97890,  // TODO
                        DestinationPickerActivity.makeRestartActivityIntent(
                            DestinationPickerActivity.Args.FileBatch(
                                moveFiles = args.moveFiles,
                                pickerStartDestination = null,
                                notificationResources = args.notificationResources
                            ),
                            context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
        }

    fun cancelOrUpdate(newMoveFileNotificationBuilderArgs: List<NewMoveFileNotificationManager.BuilderArgs>) {
        if (newMoveFileNotificationBuilderArgs.size <= 1) {
            cancelNotification()
        } else {
            buildAndPost(newMoveFileNotificationBuilderArgs)
        }
    }
}