package com.w2sv.navigator.notifications

import android.app.NotificationChannel
import android.app.NotificationManager

/**
 * Construct a [NotificationChannel] with NotificationChannel.importance defaulting to NotificationManager.IMPORTANCE_DEFAULT.
 */
fun getNotificationChannel(
    id: String,
    name: String,
    importance: Int = NotificationManager.IMPORTANCE_DEFAULT
): NotificationChannel =
    NotificationChannel(
        id,
        name,
        importance,
    )