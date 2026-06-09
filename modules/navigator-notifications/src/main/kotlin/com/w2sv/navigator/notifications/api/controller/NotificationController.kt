package com.w2sv.navigator.notifications.api.controller

import android.app.NotificationChannel
import androidx.core.app.NotificationCompat
import com.w2sv.modules.resources.R
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.api.env.NotificationEnvironment
import com.w2sv.navigator.notifications.api.notificationBuilder

internal abstract class NotificationController<Args>(
    environment: NotificationEnvironment,
    protected val appNotificationChannel: AppNotificationChannel
) : NotificationEnvironment by environment {
    protected val channel: NotificationChannel = appNotificationChannel.notificationChannel(context)

    init {
        notificationManager.createNotificationChannel(channel)
    }

    protected open fun builder(): NotificationCompat.Builder =
        notificationBuilder(context, channel.id) {
            setSmallIcon(R.drawable.ic_app_logo_24)
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

    protected abstract fun NotificationCompat.Builder.configure(args: Args, id: Int)
}
