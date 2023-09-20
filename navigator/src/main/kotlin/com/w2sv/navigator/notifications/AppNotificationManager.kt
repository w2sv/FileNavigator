package com.w2sv.navigator.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import com.w2sv.androidutils.notifying.UniqueIds

abstract class AppNotificationManager(
    protected val notificationChannel: NotificationChannel,
    protected val notificationManager: NotificationManager,
    resourcesBaseSeed: Int
) {
    init {
        notificationManager.createNotificationChannel(notificationChannel)
    }

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