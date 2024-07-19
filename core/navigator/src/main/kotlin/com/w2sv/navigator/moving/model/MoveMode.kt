package com.w2sv.navigator.moving.model

internal enum class MoveMode {
    ManualSelection, Quick, Auto;

    val updateLastMoveDestinations: Boolean
        get() = this == ManualSelection

    val isAuto: Boolean
        get() = this == Auto
}