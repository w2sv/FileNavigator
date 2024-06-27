package com.w2sv.navigator.notifications.managers.abstrct

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.CallSuper
import androidx.core.app.NotificationCompat

internal abstract class AppNotificationManager<A : AppNotificationManager.BuilderArgs>(
    protected val notificationChannel: NotificationChannel,
    protected val notificationManager: NotificationManager,
    protected val context: Context
) {
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

    interface BuilderArgs {
        data object Empty : BuilderArgs
    }

    fun buildAndEmit(id: Int, args: A) {
        notificationManager.notify(id, buildNotification(args))
    }

    fun buildNotification(args: A): Notification =
        getBuilder(args).build()

    protected abstract fun getBuilder(args: A): Builder
}