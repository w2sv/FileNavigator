package com.w2sv.navigator.notifications

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class NotificationResources(
    val id: Int,
    val managerClassName: String
) : Parcelable {

    fun pendingIntentRequestCodes(count: Int): List<Int> =
        (id until id + count).toList()

    fun cancelNotification(context: Context) {
        CleanupNotificationResourcesBroadcastReceiver.start(
            context = context,
            notificationResources = this
        )
    }

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.NotificationResources"

        fun optionalFromIntent(intent: Intent): NotificationResources? =
            intent.getParcelableCompat<NotificationResources>(EXTRA)

        fun fromIntent(intent: Intent): NotificationResources =
            optionalFromIntent(intent)!!
    }
}
