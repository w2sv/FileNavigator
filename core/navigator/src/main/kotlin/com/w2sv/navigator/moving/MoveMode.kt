package com.w2sv.navigator.moving

import android.os.Parcelable
import com.w2sv.common.utils.DocumentUri
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed interface MoveMode : Parcelable {
    val destination: DocumentUri

    @Parcelize
    data class Manual(override val destination: DocumentUri) : MoveMode

    @Parcelize
    data class Quick(override val destination: DocumentUri) : MoveMode

    @Parcelize
    data class Auto(override val destination: DocumentUri) : MoveMode

    val updateLastMoveDestinations: Boolean
        get() = when (this) {
            is Manual -> true
            else -> false
        }
}