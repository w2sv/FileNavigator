package com.w2sv.navigator.domain.moving

import android.os.Parcelable
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface DestinationSelectionManner : Parcelable {
    val cancelNotificationEvent: CancelNotificationEvent?

    @Parcelize
    @JvmInline
    value class Picked(override val cancelNotificationEvent: CancelNotificationEvent) : DestinationSelectionManner

    @Parcelize
    @JvmInline
    value class Quick(override val cancelNotificationEvent: CancelNotificationEvent) : DestinationSelectionManner

    @Parcelize
    data object Auto : DestinationSelectionManner {
        override val cancelNotificationEvent: CancelNotificationEvent?
            get() = null
    }

    val isPicked: Boolean
        get() = this is Picked

    val isAuto: Boolean
        get() = this is Auto
}
