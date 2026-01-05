package com.w2sv.navigator.notifications.controller

import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.w2sv.core.common.R
import com.w2sv.navigator.domain.NavigatorIntents
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.notifications.AppNotification
import com.w2sv.navigator.notifications.api.NotificationEnvironment
import com.w2sv.navigator.notifications.api.SingleNotificationController
import javax.inject.Inject

internal class BatchMoveProgressNotificationController @Inject constructor(
    environment: NotificationEnvironment,
    private val navigatorIntents: NavigatorIntents
) : SingleNotificationController<NotificationEvent.BatchMoveProgress>(
    environment = environment,
    appNotification = AppNotification.BatchMoveProgress
) {
    override fun NotificationCompat.Builder.configure(args: NotificationEvent.BatchMoveProgress, id: Int) {
        setContentTitle(
            context.getString(
                R.string.move_progress_notification_title,
                args.total
            )
        )
        setSilent(true)
        setOngoing(true)
        setProgress(args.total, args.current, false)
        addAction(
            NotificationCompat.Action(
                R.drawable.ic_cancel_24,
                context.getString(R.string.cancel),
                PendingIntent.getBroadcast(
                    context,
                    0,
                    navigatorIntents.cancelBatchMove(),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                )
            )
        )
    }
}
