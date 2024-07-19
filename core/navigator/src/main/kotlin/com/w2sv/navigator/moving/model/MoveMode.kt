package com.w2sv.navigator.moving.model

internal enum class MoveMode {
    DestinationPicked, Quick, Auto;

    val updateLastMoveDestinations: Boolean
        get() = this == DestinationPicked

    val isAuto: Boolean
        get() = this == Auto
}