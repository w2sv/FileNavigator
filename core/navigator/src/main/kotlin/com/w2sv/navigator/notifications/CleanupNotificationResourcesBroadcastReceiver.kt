package com.w2sv.navigator.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.common.util.log
import com.w2sv.navigator.notifications.api.MultiInstanceNotificationManager
import com.w2sv.navigator.shared.plus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import slimber.log.i

@AndroidEntryPoint
internal class CleanupNotificationResourcesBroadcastReceiver : BroadcastReceiver() {

    @Inject
    @JvmSuppressWildcards
    lateinit var multiInstanceAppNotificationManagers: Set<MultiInstanceNotificationManager<*>>

    override fun onReceive(context: Context, intent: Intent) {
        i { "onReceive" }
        NotificationResources.Companion.optionalFromIntent(intent)
            ?.let { resources ->
                multiInstanceAppNotificationManagers
                    .first { notificationManager ->
                        resources.managerClassName == notificationManager.resourcesIdentifier
                    }
                    .log { "Cleaning up ${it.resourcesIdentifier} resources" }
                    .cancelNotification(resources.id)
            }
    }

    companion object {
        fun getIntent(context: Context, notificationResources: NotificationResources): Intent =
            Intent(
                context,
                CleanupNotificationResourcesBroadcastReceiver::class.java
            ) + notificationResources

        fun start(context: Context, notificationResources: NotificationResources) {
            context.sendBroadcast(getIntent(context, notificationResources))
        }
    }
}
