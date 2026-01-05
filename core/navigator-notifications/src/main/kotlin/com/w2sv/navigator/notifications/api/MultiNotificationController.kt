package com.w2sv.navigator.notifications.api

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.w2sv.navigator.notifications.AppNotification

internal abstract class MultiNotificationController<Args>(
    environment: NotificationEnvironment,
    appNotification: AppNotification,
    configureSummaryNotification: (NotificationCompat.Builder.(Context, Int) -> NotificationCompat.Builder)? = null
) : NotificationController<Args>(environment, appNotification.channel) {

    private val ids = MultiNotificationIds(appNotification.multiInstanceIdBase)
    private val summaryController: NotificationSummaryController? = configureSummaryNotification?.let {
        NotificationSummaryController(
            context = environment.context,
            notificationManager = environment.notificationManager,
            appNotification = appNotification,
            builder = ::builder,
            configure = it
        )
    }

    val notificationCount: Int by ids::count

    fun post(args: Args): Int {
        val id = ids.next()
        val notification = build(args, id)
        notificationManager.notify(id, notification)
        summaryController?.onPost(notificationCount)
        return id
    }

    open fun cancel(id: Int) {
        notificationManager.cancel(id)
        ids.cancel(id)
        summaryController?.onCancel(notificationCount)
    }

    fun build(args: Args, id: Int): Notification =
        builder()
            .apply { configure(args, id) }
            .build()

    override fun builder(): NotificationCompat.Builder =
        super.builder().setGroup(appNotificationChannel.name)
}
