package com.w2sv.common.notifications

import android.app.NotificationChannel
import android.content.Context
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.notifying.getNotificationManager

fun Context.createNotificationChannelAndGetNotificationBuilder(
    channel: NotificationChannel
): NotificationCompat.Builder {
    getNotificationManager().createNotificationChannel(channel)
    return getNotificationBuilder(channel.id)
}

private fun Context.getNotificationBuilder(
    channelId: String
): NotificationCompat.Builder =
    NotificationCompat.Builder(this, channelId)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)