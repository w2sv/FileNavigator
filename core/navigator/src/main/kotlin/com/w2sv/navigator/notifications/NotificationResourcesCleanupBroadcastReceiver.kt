package com.w2sv.navigator.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.navigator.notifications.managers.abstrct.MultiInstanceAppNotificationManager

internal abstract class NotificationResourcesCleanupBroadcastReceiver : BroadcastReceiver() {

    abstract val multiInstanceAppNotificationManager: MultiInstanceAppNotificationManager<*>

    override fun onReceive(context: Context?, intent: Intent?) {
        multiInstanceAppNotificationManager.cancelNotificationAndFreeResources(
            intent?.getParcelableCompat<NotificationResources>(
                NotificationResources.EXTRA
            )!!
        )
    }

    companion object {
        inline fun <reified T : NotificationResourcesCleanupBroadcastReceiver> getIntent(
            context: Context,
            notificationResources: NotificationResources
        ): Intent =
            Intent(
                context,
                T::class.java
            )
                .putExtra(
                    NotificationResources.EXTRA,
                    notificationResources
                )

        /**
         * @param intent Intent, whose extras contain the [NotificationResources].
         */
        inline fun <reified T : NotificationResourcesCleanupBroadcastReceiver> startFromResourcesComprisingIntent(
            context: Context,
            intent: Intent
        ) {
            context.sendBroadcast(
                intent.setClass(context, T::class.java)
            )
        }
    }
}