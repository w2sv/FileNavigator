package com.w2sv.navigator.notifications.api

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.CallSuper
import com.w2sv.androidutils.UniqueIds
import com.w2sv.navigator.notifications.CleanupNotificationResourcesBroadcastReceiver
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.appnotifications.AppNotificationChannel
import com.w2sv.navigator.notifications.appnotifications.AppNotificationId
import slimber.log.i

/**
 * Manager for notifications of which several instances may be active at the same time.
 */
internal abstract class MultiInstanceNotificationManager<A : MultiInstanceNotificationManager.Args>(
    appNotificationChannel: AppNotificationChannel,
    notificationManager: NotificationManager,
    context: Context,
    appNotificationId: AppNotificationId
) : AppNotificationManager<A>(
    appNotificationChannel = appNotificationChannel,
    notificationManager = notificationManager,
    context = context
) {
    val resourcesIdentifier: String
        get() = this::class.java.simpleName

    private val notificationIds = UniqueIds(appNotificationId.multiInstanceIdBase)

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
                CleanupNotificationResourcesBroadcastReceiver.getIntent(
                    context,
                    notificationResources
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
    }

    interface Args {
        val notificationResources: NotificationResources
    }

    protected fun getNotificationResources(): NotificationResources =
        NotificationResources(
            id = notificationIds.addNewId(),
            managerClassName = resourcesIdentifier
        )

    @CallSuper
    open fun buildAndPost(args: A) {
        buildAndPostNotification(args.notificationResources.id, args)
    }

    // ================
    // Cancelling
    // ================

    @CallSuper
    open fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
        notificationIds.remove(id)

        i { "Post-freeNotificationResources: NotificationIds: $notificationIds" }
    }
}
