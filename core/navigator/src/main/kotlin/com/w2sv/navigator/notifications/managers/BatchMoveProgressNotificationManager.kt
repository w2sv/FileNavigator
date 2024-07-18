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

internal class BatchMoveProgressNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager,
) : SingleInstanceAppNotificationManager<BatchMoveProgressNotificationManager.BuilderArgs>(
    appNotificationChannel = AppNotificationChannel.MoveProgress,
    notificationManager = notificationManager,
    context = context,
    notificationId = 97892
) {
    data class BuilderArgs(
        val current: Int,
        val max: Int
    ) : AppNotificationManager.BuilderArgs

    fun buildAndPostNotification(
        current: Int,
        max: Int
    ) {
        buildAndPostNotification(BuilderArgs(current, max))
    }

    override fun getBuilder(args: BuilderArgs): AppNotificationManager<BuilderArgs>.Builder {
        return object : Builder() {
            override fun build(): Notification {
                setContentTitle(context.getString(R.string.move_progress))
                setSilent(true)
                setProgress(args.max, args.current, false)

                return super.build()
            }
        }
    }
}