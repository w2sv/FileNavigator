package com.w2sv.common.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.notifying.getNotificationManager

fun Context.createNotificationChannelAndGetNotificationBuilder(
    channel: NotificationChannelProperties
): NotificationCompat.Builder {
    getNotificationManager().createNotificationChannel(
        NotificationChannel(
            channel.id,
            channel.name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
    )
    return notificationBuilder(channel.id)
}

private fun Context.notificationBuilder(
    channelId: String
): NotificationCompat.Builder =
    NotificationCompat.Builder(this, channelId)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)