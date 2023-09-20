package com.w2sv.navigator.notifications

import android.app.NotificationChannel
import android.app.NotificationManager

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