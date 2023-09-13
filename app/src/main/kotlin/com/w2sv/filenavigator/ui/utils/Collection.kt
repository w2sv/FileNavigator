package com.w2sv.filenavigator.ui.utils

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.w2sv.androidutils.coroutines.getSynchronousMap
import kotlinx.coroutines.flow.Flow

// =============
// Map
// =============

fun <K, V> Map<K, V>.getMutableStateMap(): SnapshotStateMap<K, V> =
    mutableStateMapOf<K, V>()
        .apply { putAll(this@getMutableStateMap) }

fun <K, V> Map<K, Flow<V>>.getSynchronousMutableStateMap(): SnapshotStateMap<K, V> =
    getSynchronousMap().getMutableStateMap()

// =============
// Iterable
// =============

fun Iterable<Boolean>.allFalseAfterEnteringValue(newValue: Boolean): Boolean =
    !newValue && count { it } <= 1

fun <T> Iterable<T>.getMutableStateList(): SnapshotStateList<T> =
    SnapshotStateList<T>().apply {
        addAll(this@getMutableStateList)
    }