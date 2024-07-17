package com.w2sv.navigator.notifications.managers

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.w2sv.core.navigator.R
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.SingleInstanceAppNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BatchMoveNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager,
) : SingleInstanceAppNotificationManager<BatchMoveNotificationManager.BuilderArgs>(
    notificationChannel = AppNotificationChannel.BatchMoveFiles.getNotificationChannel(context),
    notificationManager = notificationManager,
    context = context,
    notificationId = 9867234
) {
    data class BuilderArgs(
        val fileCount: Int
    ) : AppNotificationManager.BuilderArgs

    fun buildAndEmit(fileCount: Int) {
        buildAndPostNotification(BuilderArgs(fileCount))
    }

    override fun getBuilder(args: BuilderArgs): Builder =
        object : Builder() {
            override fun build(): Notification {
                setGroup(notificationChannel.id)
                setContentTitle(context.getString(R.string.move_files, args.fileCount))
                setSilent(true)
                return super.build()
            }

//            private fun getMoveFilesAction(
//                requestCode: Int,
//            ): NotificationCompat.Action =
//                NotificationCompat.Action(
//                    com.w2sv.core.common.R.drawable.ic_app_logo_24,
//                    context.getString(R.string.move),
//                    PendingIntent.getActivity(
//                        context,
//                        requestCode,
//                        MoveDestinationSelectionActivity.makeRestartActivityIntent(
//                            moveFile = args.moveFile,
//                            notificationResources = args.resources,
//                            context = context
//                        ),
//                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//                    )
//                )
        }

    fun cancelOrUpdate(newMoveFileNotificationCount: Int) {
        if (newMoveFileNotificationCount <= 1) {
            cancelNotification()
        } else {
            buildAndEmit(newMoveFileNotificationCount)
        }
    }
}