package com.w2sv.navigator.domain.notifications

import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat

sealed interface CancelNotificationEvent :
    NotificationEvent,
    Parcelable {
    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.CancelNotificationEvent"

        operator fun invoke(intent: Intent): CancelNotificationEvent =
            checkNotNull(optional(intent))

        fun optional(intent: Intent): CancelNotificationEvent? =
            intent.getParcelableCompat<CancelNotificationEvent>(EXTRA)
    }
}
