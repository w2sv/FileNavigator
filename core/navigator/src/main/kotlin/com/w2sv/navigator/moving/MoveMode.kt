package com.w2sv.navigator.moving

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed interface MoveMode : Parcelable {
    val destination: Uri

    @Parcelize
    data class Manual(override val destination: Uri) : MoveMode

    @Parcelize
    data class Quick(override val destination: Uri) : MoveMode

    @Parcelize
    data class Auto(override val destination: Uri) : MoveMode

    val updateLastMoveDestinations: Boolean
        get() = when (this) {
            is Manual -> true
            else -> false
        }
}