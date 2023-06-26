package com.w2sv.filenavigator.navigator.notifications

import android.content.Context
import android.os.Parcelable
import com.w2sv.androidutils.notifying.getNotificationManager
import com.w2sv.filenavigator.navigator.service.FileNavigatorService
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationParameters(
    val notificationId: Int,
    val associatedRequestCodes: ArrayList<Int>
) : Parcelable {

    fun cancelUnderlyingNotification(context: Context) {
        context.getNotificationManager().cancel(notificationId)

        FileNavigatorService.onNotificationCancelled(
            this,
            context
        )
    }

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.NOTIFICATION_PARAMETERS"
    }
}