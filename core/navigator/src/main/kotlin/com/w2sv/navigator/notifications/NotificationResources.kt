package com.w2sv.navigator.notifications

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class NotificationResources(
    val id: Int,
    val actionRequestCodes: ArrayList<Int>
) : Parcelable {

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.NOTIFICATION_PARAMETERS"
    }
}