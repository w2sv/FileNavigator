package com.w2sv.navigator.notifications.api

import android.app.NotificationChannel
import androidx.core.app.NotificationCompat
import com.w2sv.core.common.R
import com.w2sv.navigator.notifications.AppNotificationChannel

internal abstract class NotificationController<Args>(
    environment: NotificationEnvironment,
    protected val appNotificationChannel: AppNotificationChannel
) {
    protected val context = environment.context
    protected val notificationManager = environment.notificationManager

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
