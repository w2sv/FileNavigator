package com.w2sv.navigator.notifications.api

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.w2sv.navigator.notifications.AppNotification
import slimber.log.i

internal class NotificationSummaryController(
    private val context: Context,
    private val notificationManager: NotificationManager,
    appNotification: AppNotification,
    private val builder: () -> NotificationCompat.Builder,
    private val configure: NotificationCompat.Builder.(Context, Int) -> NotificationCompat.Builder
) {
    private val id by appNotification::summaryId

    fun onPost(activeNotifications: Int) {
        if (activeNotifications >= 2) {
            i { "Posting summary" }
            post(activeNotifications)
        }
    }

    fun onCancel(activeNotifications: Int) {
        if (activeNotifications == 0) {
            i { "Cancelling summary" }
            notificationManager.cancel(id)
        } else {
            i { "Updating summary" }
            post(activeNotifications)
        }
    }

    private fun post(activeNotifications: Int) {
        notificationManager.notify(id, notification(activeNotifications))
    }

    private fun notification(activeNotifications: Int): Notification =
        builder()
            .configure(context, activeNotifications)
            .setGroupSummary(true)
            .build()
}
