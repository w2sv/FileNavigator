package com.w2sv.navigator.domain.moving

import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent
import kotlinx.parcelize.Parcelize

@Parcelize
data class MoveFileNotificationData(val moveFile: MoveFile, val cancelNotificationEvent: CancelNotificationEvent) : Parcelable {
    companion object {
        const val EXTRA = "com.w2sv.filenavigator.MoveFileNotificationData"

        operator fun invoke(intent: Intent): MoveFileNotificationData =
            checkNotNull(intent.getParcelableCompat<MoveFileNotificationData>(EXTRA))
    }
}
