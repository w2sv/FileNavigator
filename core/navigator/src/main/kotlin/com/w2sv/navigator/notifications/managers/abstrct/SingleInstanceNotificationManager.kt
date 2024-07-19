package com.w2sv.navigator.notifications.managers.abstrct

import android.app.NotificationManager
import android.content.Context
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.AppNotificationId

/**
 * Manager for notifications of which only a single instance may be active at a time.
 */
internal abstract class SingleInstanceNotificationManager<A : AppNotificationManager.BuilderArgs>(
    appNotificationChannel: AppNotificationChannel,
    notificationManager: NotificationManager,
    context: Context,
    private val appNotificationId: AppNotificationId
) : AppNotificationManager<A>(
    appNotificationChannel = appNotificationChannel,
    notificationManager = notificationManager,
    context = context
) {
    fun buildAndPostNotification(args: A) {
        notificationManager.notify(appNotificationId.id, buildNotification(args))
    }

    fun cancelNotification() {
        notificationManager.cancel(appNotificationId.id)
    }
}