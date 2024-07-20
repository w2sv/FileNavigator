package com.w2sv.navigator.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.navigator.notifications.managers.AutoMoveDestinationInvalidNotificationManager
import com.w2sv.navigator.notifications.managers.MoveFileNotificationManager
import com.w2sv.navigator.shared.putOptionalNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import slimber.log.i
import javax.inject.Inject

@Parcelize
internal data class NotificationResources(
    val id: Int,
    val pendingIntentRequestCodes: ArrayList<Int>,
    private val resourcesIdentifier: String
) : Parcelable {

    fun cancelNotification(context: Context) {
        CleanupBroadcastReceiver.start(
            context = context,
            notificationResources = this
        )
    }

    @AndroidEntryPoint
    class CleanupBroadcastReceiver : BroadcastReceiver() {

        @Inject
        lateinit var moveFileNotificationManager: MoveFileNotificationManager

        @Inject
        lateinit var autoMoveDestinationInvalidNotificationManager: AutoMoveDestinationInvalidNotificationManager

        private val notificationManagers by lazy {
            listOf(
                moveFileNotificationManager, autoMoveDestinationInvalidNotificationManager
            )
        }

        override fun onReceive(context: Context, intent: Intent) {
            fromIntent(intent)
                ?.let { resources ->
                    notificationManagers.first { notificationManager ->
                        resources.resourcesIdentifier == notificationManager.resourcesIdentifier
                    }
                        .also { i { "Cleaning up ${it.resourcesIdentifier} resources" } }
                        .cancelNotificationAndFreeResources(resources)
                }
        }

        companion object {
            fun getIntent(
                context: Context,
                notificationResources: NotificationResources
            ): Intent =
                Intent(
                    context,
                    CleanupBroadcastReceiver::class.java
                )
                    .putExtra(
                        EXTRA,
                        notificationResources
                    )

            fun start(context: Context, notificationResources: NotificationResources) {
                context.sendBroadcast(
                    Intent(context, CleanupBroadcastReceiver::class.java)
                        .putOptionalNotificationResourcesExtra(notificationResources)
                )
            }
        }
    }

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.NotificationResources"

        fun fromIntent(intent: Intent): NotificationResources? =
            intent.getParcelableCompat<NotificationResources>(EXTRA)
    }
}