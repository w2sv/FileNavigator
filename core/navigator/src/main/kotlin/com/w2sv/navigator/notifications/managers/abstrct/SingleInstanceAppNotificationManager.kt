package com.w2sv.navigator.notifications.managers.abstrct

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

internal abstract class SingleInstanceAppNotificationManager<A : AppNotificationManager.BuilderArgs>(
    notificationChannel: NotificationChannel,
    notificationManager: NotificationManager,
    context: Context,
    private val notificationId: Int
) : AppNotificationManager<A>(
    notificationChannel = notificationChannel,
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