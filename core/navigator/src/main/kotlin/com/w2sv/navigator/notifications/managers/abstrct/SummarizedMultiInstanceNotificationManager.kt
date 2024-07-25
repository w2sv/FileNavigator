package com.w2sv.navigator.notifications.managers.abstrct

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.CallSuper
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.AppNotificationId
import slimber.log.i

/**
 * Manager for multi-instance notifications that post a summary.
 */
internal abstract class SummarizedMultiInstanceNotificationManager<A : MultiInstanceNotificationManager.Args>(
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
    private val summaryNotificationId = appNotificationId.summaryId

    /**
     * Implicitly cancels or updates the summary notification depending on the number of active
     * notifications after cancelling the one specified by [id].
     */
    @CallSuper
    override fun cancelNotification(id: Int) {
        super.cancelNotification(id)

        if (activeNotificationCount == 0) {
            i { "Cancelling summary" }
            notificationManager.cancel(summaryNotificationId)
        } else {
            // Update notification
            postSummaryNotification()
        }
    }

    /**
     * Implicitly posts the summary notification if there are at least 2 active notifications after
     * posting the one specified by [args].
     */
    @CallSuper
    override fun buildAndPost(args: A) {
        super.buildAndPost(args)

        if (activeNotificationCount >= 2) {
            postSummaryNotification()
        }
    }

    private fun postSummaryNotification() {
        notificationManager.notify(summaryNotificationId, buildSummaryNotification())
    }

    abstract fun buildSummaryNotification(): Notification
}