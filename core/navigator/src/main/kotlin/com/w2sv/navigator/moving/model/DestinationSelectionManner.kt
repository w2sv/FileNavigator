package com.w2sv.navigator.moving.model

import android.os.Parcelable
import com.w2sv.navigator.notifications.NotificationResources
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed interface DestinationSelectionManner : Parcelable {

    sealed interface NotificationBased : DestinationSelectionManner {
        val notificationResources: NotificationResources
    }

    @Parcelize
    data class Picked(
        override val notificationResources: NotificationResources,
    ) : NotificationBased

    @Parcelize
    data class Quick(
        override val notificationResources: NotificationResources,
    ) : NotificationBased

    @Parcelize
    data object Auto : DestinationSelectionManner

    val updateLastMoveDestinations: Boolean
        get() = this is Picked

    val isAuto: Boolean
        get() = this is Auto
}