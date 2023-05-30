package com.w2sv.filenavigator.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.notifying.getNotificationManager

fun Context.createNotificationChannelAndGetNotificationBuilder(
    channel: AppNotificationChannel,
    contentTitle: String? = null
): NotificationCompat.Builder {
    getNotificationManager().createNotificationChannel(
        NotificationChannel(
            channel.name,
            getString(channel.titleRes),
            NotificationManager.IMPORTANCE_DEFAULT
        )
    )
    return notificationBuilder(channel.name, contentTitle)
}

private fun Context.notificationBuilder(
    channelId: String,
    title: String?,
): NotificationCompat.Builder =
    NotificationCompat.Builder(this, channelId)
        .setContentTitle(title)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)