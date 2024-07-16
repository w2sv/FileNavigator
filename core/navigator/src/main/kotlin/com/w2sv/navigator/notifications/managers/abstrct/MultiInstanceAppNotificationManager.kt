package com.w2sv.navigator.notifications.managers.abstrct

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.CallSuper
import com.w2sv.androidutils.UniqueIds
import com.w2sv.navigator.notifications.NotificationResources
import slimber.log.i

internal abstract class MultiInstanceAppNotificationManager<A : MultiInstanceAppNotificationManager.BuilderArgs>(
    notificationChannel: NotificationChannel,
    notificationManager: NotificationManager,
    context: Context,
    resourcesBaseSeed: Int,
    private val summaryProperties: SummaryProperties? = null,
) : AppNotificationManager<A>(
    notificationChannel = notificationChannel,
    notificationManager = notificationManager,
    context = context
) {
    val resourcesIdentifier: String
        get() = this::class.java.simpleName

    data class SummaryProperties(val id: Int)

    private val notificationIds = UniqueIds(resourcesBaseSeed)
    private val pendingIntentRequestCodes = UniqueIds(resourcesBaseSeed)

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

    abstract class BuilderArgs(val resources: NotificationResources) : AppNotificationManager.BuilderArgs

    protected fun getNotificationResources(pendingIntentRequestCodeCount: Int): NotificationResources =
        NotificationResources(
            id = notificationIds.addNewId(),
            pendingIntentRequestCodes = pendingIntentRequestCodes.addMultipleNewIds(
                pendingIntentRequestCodeCount
            ),
            resourcesIdentifier = resourcesIdentifier.also { i { "Putting identifier=$resourcesIdentifier" } }
        )

    @CallSuper
    open fun buildAndEmit(args: A) {
        super.buildAndPostNotification(args.resources.id, args)

        if (activeNotificationCount >= 2 && summaryProperties != null) {
            emitSummaryNotification()
        }
    }

    private fun emitSummaryNotification() {
        requireNotNull(summaryProperties)

        notificationManager.notify(summaryProperties.id, buildSummaryNotification())
    }

    open fun buildSummaryNotification(): Notification? = null

    // ================
    // Cancelling
    // ================

    @CallSuper
    open fun cancelNotificationAndFreeResources(resources: NotificationResources) {
        notificationManager.cancel(resources.id)
        freeNotificationResources(resources)
        if (activeNotificationCount == 0 && summaryProperties != null) {
            notificationManager.cancel(summaryProperties.id)
        } else {
            emitSummaryNotification()
        }
    }

    private fun freeNotificationResources(resources: NotificationResources) {
        notificationIds.remove(resources.id)
        pendingIntentRequestCodes.removeAll(resources.pendingIntentRequestCodes.toSet())

        i { "Post-freeNotificationResources: NotificationIds: $notificationIds | pendingIntentRequestCodes: $pendingIntentRequestCodes" }
    }
}