package com.w2sv.navigator.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.w2sv.common.logging.LoggingBroadcastReceiver
import com.w2sv.common.util.intent
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class NotificationEventReceiver : LoggingBroadcastReceiver() {

    @Inject
    lateinit var eventHandler: NotificationEventHandler

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        eventHandler(CancelNotificationEvent(intent))
    }

    companion object {
        fun pendingIntent(context: Context, requestCode: Int, event: CancelNotificationEvent): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent(context, event),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )

        fun intent(context: Context, event: CancelNotificationEvent): Intent =
            intent<NotificationEventReceiver>(context)
                .putExtra(CancelNotificationEvent.EXTRA, event)
    }
}
