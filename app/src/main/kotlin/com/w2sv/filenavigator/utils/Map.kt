package com.w2sv.filenavigator.utils

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

fun <K, V> Map<K, V>.getMutableStateMap(): SnapshotStateMap<K, V> =
    mutableStateMapOf<K, V>()
        .apply { putAll(this@getMutableStateMap) }

fun <K> MutableMap<K, Boolean>.toggle(key: K) {
    this[key] = !getValue(key)
}