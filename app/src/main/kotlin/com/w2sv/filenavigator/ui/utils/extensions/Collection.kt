package com.w2sv.filenavigator.ui.utils.extensions

// =============
// Map
// =============

fun <T> MutableMap<T, Boolean>.toggle(key: T) {
    put(key, !getValue(key))
}

// =============
// Iterable
// =============

fun Iterable<Boolean>.allFalseAfterEnteringValue(newValue: Boolean): Boolean =
    !newValue && count { it } <= 1