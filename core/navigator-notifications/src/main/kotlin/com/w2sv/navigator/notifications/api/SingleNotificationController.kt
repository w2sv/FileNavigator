package com.w2sv.navigator.notifications.api

import android.app.Notification
import com.w2sv.navigator.notifications.AppNotification

internal abstract class SingleNotificationController<Args>(environment: NotificationEnvironment, appNotification: AppNotification) :
    NotificationController<Args>(environment, appNotification.channel) {

    val id = appNotification.id

    fun post(args: Args) {
        val notification = build(args)
        notificationManager.notify(id, notification)
    }

    fun cancel() {
        notificationManager.cancel(id)
    }

    fun build(args: Args): Notification =
        builder()
            .apply { configure(args, id) }
            .build()
}
