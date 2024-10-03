package com.w2sv.navigator.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.util.log
import com.w2sv.navigator.notifications.managers.AutoMoveDestinationInvalidNotificationManager
import com.w2sv.navigator.notifications.managers.MoveFileNotificationManager
import com.w2sv.navigator.shared.putOptionalNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
internal data class NotificationResources(
    val id: Int,
    private val managerClassName: String
) : Parcelable {

    fun pendingIntentRequestCodes(count: Int): List<Int> =
        (id until id + count).toList()

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

        override fun onReceive(context: Context, intent: Intent) {
            fromIntent(intent)
                ?.let { resources ->
                    listOf(
                        moveFileNotificationManager, autoMoveDestinationInvalidNotificationManager
                    )
                        .first { notificationManager ->
                            resources.managerClassName == notificationManager.resourcesIdentifier
                        }
                        .log { "Cleaning up ${it.resourcesIdentifier} resources" }
                        .cancelNotification(resources.id)
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