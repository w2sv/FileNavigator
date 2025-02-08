package com.w2sv.navigator.moving.model

import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.navigator.notifications.NotificationResources
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MoveFileWithNotificationResources(
    val moveFile: MoveFile,
    val notificationResources: NotificationResources
) : Parcelable {

    fun addToIntent(intent: Intent): Intent =
        intent.putExtra(EXTRA, this)

    companion object {
        fun fromIntent(intent: Intent): MoveFileWithNotificationResources =
            intent.getParcelableCompat(EXTRA)!!

        private const val EXTRA = "com.w2sv.filenavigator.extra.MoveFileWithNotificationResources"
    }
}
