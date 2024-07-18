package com.w2sv.navigator.notifications.managers.abstrct

import android.app.NotificationManager
import android.content.Context
import com.w2sv.navigator.notifications.AppNotificationChannel

internal abstract class SingleInstanceAppNotificationManager<A : AppNotificationManager.BuilderArgs>(
    appNotificationChannel: AppNotificationChannel,
    notificationManager: NotificationManager,
    context: Context,
    private val notificationId: Int
) : AppNotificationManager<A>(
    appNotificationChannel = appNotificationChannel,
    notificationManager = notificationManager,
    context = context
) {
    fun buildAndPostNotification(args: A) {
        notificationManager.notify(notificationId, buildNotification(args))
    }

    fun cancelNotification() {
        notificationManager.cancel(notificationId)
    }
}