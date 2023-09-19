package com.w2sv.navigator.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager

fun NotificationManager.notify(channel: NotificationChannel, id: Int, notification: Notification) {
    createNotificationChannel(channel)
    notify(id, notification)
}

fun getNotificationChannel(
    id: String,
    name: String,
    importance: Int = NotificationManager.IMPORTANCE_DEFAULT
): NotificationChannel =
    NotificationChannel(
        id,
        name,
        importance
    )