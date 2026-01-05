package com.w2sv.navigator.notifications.api

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat

internal fun buildNotification(context: Context, channelId: String, configure: NotificationCompat.Builder.() -> Unit): Notification =
    notificationBuilder(context, channelId, configure).build()

internal fun notificationBuilder(
    context: Context,
    channelId: String,
    configure: NotificationCompat.Builder.() -> Unit
): NotificationCompat.Builder =
    NotificationCompat.Builder(context, channelId)
        .apply { configure() }

internal fun NotificationCompat.Builder.setBigTextStyle(text: CharSequence?): NotificationCompat.Builder =
    setStyle(NotificationCompat.BigTextStyle().bigText(text))
