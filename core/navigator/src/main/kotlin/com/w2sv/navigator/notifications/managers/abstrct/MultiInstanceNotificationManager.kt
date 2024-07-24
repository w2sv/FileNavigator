package com.w2sv.navigator.notifications.managers.abstrct

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.CallSuper
import com.w2sv.androidutils.UniqueIds
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.AppNotificationId
import com.w2sv.navigator.notifications.NotificationResources
import slimber.log.i

/**
 * Manager for notifications of which several instances may be active at the same time.
 */
internal abstract class MultiInstanceNotificationManager<A : MultiInstanceNotificationManager.Args>(
    appNotificationChannel: AppNotificationChannel,
    notificationManager: NotificationManager,
    context: Context,
    appNotificationId: AppNotificationId,
) : AppNotificationManager<A>(
    appNotificationChannel = appNotificationChannel,
    notificationManager = notificationManager,
    context = context
) {
    val resourcesIdentifier: String
        get() = this::class.java.simpleName

    private val notificationIds = UniqueIds(appNotificationId.multiInstanceIdBase)
    private val pendingIntentRequestCodes = UniqueIds(appNotificationId.multiInstanceIdBase)

    protected val activeNotificationCount: Int
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

        protected fun getCleanupNotificationResourcesPendingIntent(
            requestCode: Int,
            notificationResources: NotificationResources
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                NotificationResources.CleanupBroadcastReceiver.getIntent(
                    context,
                    notificationResources
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
    }

    interface Args {
        val resources: NotificationResources
    }

    protected fun getNotificationResources(pendingIntentRequestCodeCount: Int): NotificationResources =
        NotificationResources(
            id = notificationIds.addNewId(),
            pendingIntentRequestCodes = pendingIntentRequestCodes.addNewIds(
                pendingIntentRequestCodeCount
            ),
            resourcesIdentifier = resourcesIdentifier
        )

    @CallSuper
    open fun buildAndPost(args: A) {
        buildAndPostNotification(args.resources.id, args)
    }

    // ================
    // Cancelling
    // ================

    @CallSuper
    open fun cancelNotificationAndFreeResources(resources: NotificationResources) {
        notificationManager.cancel(resources.id)
        freeNotificationResources(resources)
    }

    private fun freeNotificationResources(resources: NotificationResources) {
        notificationIds.remove(resources.id)
        pendingIntentRequestCodes.removeAll(resources.pendingIntentRequestCodes.toSet())

        i { "Post-freeNotificationResources: NotificationIds: $notificationIds | pendingIntentRequestCodes: $pendingIntentRequestCodes" }
    }
}