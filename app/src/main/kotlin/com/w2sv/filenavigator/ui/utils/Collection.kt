package com.w2sv.filenavigator.ui.utils

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap

// =============
// Map
// =============

fun <K, V> Map<K, V>.getMutableStateMap(): SnapshotStateMap<K, V> =
    mutableStateMapOf<K, V>()
        .apply { putAll(this@getMutableStateMap) }

fun <K> MutableMap<K, Boolean>.toggle(key: K) {
    this[key] = !getValue(key)
}

// =============
// Iterable
// =============

fun Iterable<Boolean>.allFalseAfterEnteringValue(newValue: Boolean): Boolean =
    !newValue && count { it } <= 1

fun <T> Iterable<T>.getMutableStateList(): SnapshotStateList<T> =
    SnapshotStateList<T>().apply {
        addAll(this@getMutableStateList)
    }