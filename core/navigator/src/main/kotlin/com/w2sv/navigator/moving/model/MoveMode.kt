package com.w2sv.navigator.moving.model

import android.os.Parcelable
import com.w2sv.navigator.notifications.NotificationResources
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed interface MoveMode : Parcelable {

    sealed interface NotificationBased : MoveMode {
        val notificationResources: NotificationResources
    }

    sealed interface Batchable : NotificationBased {
        val isPartOfBatch: Boolean
    }

    @Parcelize
    data class DestinationPicked(
        override val notificationResources: NotificationResources,
        override val isPartOfBatch: Boolean = false
    ) :
        NotificationBased, Batchable

    @Parcelize
    data class Quick(
        override val notificationResources: NotificationResources,
        override val isPartOfBatch: Boolean = false
    ) : NotificationBased, Batchable

    @Parcelize
    data object Auto : MoveMode

    val updateLastMoveDestinations: Boolean
        get() = this is DestinationPicked

    val isAuto: Boolean
        get() = this == Auto

    val showMoveResultToast: Boolean
        get() = this !is Batchable || !isPartOfBatch
}