package com.w2sv.navigator.notifications

import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.generic.getParcelableCompat
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class NotificationResources(
    val id: Int,
    val pendingIntentRequestCodes: ArrayList<Int>
) : Parcelable {

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.NOTIFICATION_PARAMETERS"

        fun fromIntent(intent: Intent): NotificationResources? =
            intent.getParcelableCompat<NotificationResources>(EXTRA)
    }
}