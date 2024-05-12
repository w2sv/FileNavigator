package com.w2sv.navigator.notifications.managers.abstrct

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.CallSuper
import com.w2sv.androidutils.notifying.UniqueIds
import com.w2sv.navigator.notifications.NotificationResources
import slimber.log.i

internal abstract class MultiInstanceAppNotificationManager<A : MultiInstanceAppNotificationManager.BuilderArgs>(
    notificationChannel: NotificationChannel,
    notificationManager: NotificationManager,
    context: Context,
    resourcesBaseSeed: Int,
    private val summaryId: Int,
) : AppNotificationManager<A>(notificationChannel, notificationManager, context) {

    private val notificationIds = UniqueIds(resourcesBaseSeed)
    private val pendingIntentRequestCodes = UniqueIds(resourcesBaseSeed)

    protected val nActiveNotifications: Int
        get() = notificationIds.size

    // =============
    // Building
    // =============

    open inner class Builder : AppNotificationManager<A>.Builder() {

        @CallSuper
        override fun build(): Notification {
            setGroup(notificationChannel.id)

            return super.build()
        }
    }

    abstract class BuilderArgs(val resources: NotificationResources) :
        AppNotificationManager.BuilderArgs

    protected fun getNotificationResources(nPendingRequestCodes: Int): NotificationResources =
        NotificationResources(
            notificationIds.addNewId(),
            pendingIntentRequestCodes.addMultipleNewIds(nPendingRequestCodes)
        )

    fun buildAndEmit(args: A) {
        super.buildAndEmit(args.resources.id, args)

        if (nActiveNotifications >= 2) {
            notificationManager.notify(summaryId, buildSummaryNotification())
        }
    }

    abstract fun buildSummaryNotification(): Notification

    // ================
    // Cancelling
    // ================

    fun cancelNotificationAndFreeResources(resources: NotificationResources) {
        notificationManager.cancel(resources.id)
        freeNotificationResources(resources)
    }

    private fun freeNotificationResources(resources: NotificationResources) {
        notificationIds.remove(resources.id)
        pendingIntentRequestCodes.removeAll(resources.actionRequestCodes.toSet())

        i { "Post-freeNotificationResources: NotificationIds: $notificationIds | pendingIntentRequestCodes: $pendingIntentRequestCodes" }
    }
}