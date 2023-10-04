package com.w2sv.navigator.notifications.managers.abstrct

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.CallSuper
import androidx.core.app.NotificationCompat

abstract class AppNotificationManager<A: AppNotificationManager.Args>(
    protected val notificationChannel: NotificationChannel,
    protected val notificationManager: NotificationManager,
    protected val context: Context
) {
    init {
        notificationManager.createNotificationChannel(notificationChannel)
    }

    interface Args {
        data object Empty: Args
    }

    abstract inner class Builder: NotificationCompat.Builder(context, notificationChannel.id) {

        @CallSuper
        override fun build(): Notification {
            priority = NotificationCompat.PRIORITY_DEFAULT

            return super.build()
        }
    }

    protected abstract fun getBuilder(args: A): Builder

    fun buildNotification(args: A): Notification =
        getBuilder(args).build()

    fun buildAndEmit(id: Int, args: A) {
        notificationManager.notify(id, buildNotification(args))
    }
}