package com.w2sv.navigator.notifications.appnotificationmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.w2sv.androidutils.notifying.UniqueIds
import com.w2sv.navigator.notifications.NotificationResources

abstract class MultiInstanceAppNotificationManager<A : AppNotificationManager.Args>(
    notificationChannel: NotificationChannel,
    notificationManager: NotificationManager,
    context: Context,
    resourcesBaseSeed: Int
) : AppNotificationManager<A>(notificationChannel, notificationManager, context) {

    private val notificationIds = UniqueIds(resourcesBaseSeed)
    private val pendingIntentRequestCodes = UniqueIds(resourcesBaseSeed)

    protected fun getNotificationResources(nPendingRequestCodes: Int): NotificationResources =
        NotificationResources(
            notificationIds.addNewId(),
            pendingIntentRequestCodes.addMultipleNewIds(nPendingRequestCodes)
        )

    fun cancelNotification(resources: NotificationResources) {
        notificationManager.cancel(resources.id)
        freeNotificationResources(resources)
    }

    private fun freeNotificationResources(resources: NotificationResources) {
        notificationIds.remove(resources.id)
        pendingIntentRequestCodes.removeAll(resources.actionRequestCodes.toSet())
    }
}