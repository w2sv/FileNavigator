package com.w2sv.navigator.notifications.managers.abstrct

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.CallSuper
import androidx.core.app.NotificationCompat
import com.w2sv.navigator.notifications.AppNotificationChannel

internal abstract class AppNotificationManager<Args>(
    appNotificationChannel: AppNotificationChannel,
    protected val notificationManager: NotificationManager,
    protected val context: Context
) {
    protected val notificationChannel: NotificationChannel =
        appNotificationChannel.getNotificationChannel(context)

    init {
        notificationManager.createNotificationChannel(notificationChannel)
    }

    open inner class Builder : NotificationCompat.Builder(context, notificationChannel.id) {

        @CallSuper
        override fun build(): Notification {
            setSmallIcon(com.w2sv.core.common.R.drawable.ic_app_logo_24)

            priority = NotificationCompat.PRIORITY_DEFAULT

            return super.build()
        }
    }

    protected fun buildAndPostNotification(id: Int, args: Args) {
        notificationManager.notify(id, buildNotification(args))
    }

    fun buildNotification(args: Args): Notification =
        getBuilder(args).build()

    protected abstract fun getBuilder(args: Args): Builder
}
