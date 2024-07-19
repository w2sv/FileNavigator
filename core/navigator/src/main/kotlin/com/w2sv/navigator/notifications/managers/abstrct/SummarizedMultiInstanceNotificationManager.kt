package com.w2sv.navigator.notifications.managers.abstrct

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.CallSuper
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.AppNotificationId
import com.w2sv.navigator.notifications.NotificationResources

internal abstract class SummarizedMultiInstanceNotificationManager<A : MultiInstanceNotificationManager.BuilderArgs>(
    appNotificationChannel: AppNotificationChannel,
    notificationManager: NotificationManager,
    context: Context,
    appNotificationId: AppNotificationId,
) : MultiInstanceNotificationManager<A>(
    appNotificationChannel = appNotificationChannel,
    notificationManager = notificationManager,
    context = context,
    appNotificationId = appNotificationId
) {
    private val summaryNotificationId = appNotificationId.id + 999  // TODO

    private fun emitSummaryNotification() {
        notificationManager.notify(summaryNotificationId, buildSummaryNotification())
    }

    abstract fun buildSummaryNotification(): Notification?

    @CallSuper
    override fun cancelNotificationAndFreeResources(resources: NotificationResources) {
        super.cancelNotificationAndFreeResources(resources)

        if (activeNotificationCount == 0) {
            notificationManager.cancel(summaryNotificationId)
        } else {
            // Update notification
            emitSummaryNotification()
        }
    }

    @CallSuper
    override fun buildAndPost(args: A) {
        super.buildAndPost(args)

        if (activeNotificationCount >= 2) {
            emitSummaryNotification()
        }
    }
}