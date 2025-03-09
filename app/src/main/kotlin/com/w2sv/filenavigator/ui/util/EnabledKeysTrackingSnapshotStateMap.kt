package com.w2sv.filenavigator.ui.util

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList

@Stable
class EnabledKeysTrackingSnapshotStateMap<K>(private val map: SnapshotStateMap<K, Boolean> = mutableStateMapOf()) :
    MutableMap<K, Boolean> by map {

    val enabledKeys: SnapshotStateList<K> = keys.filter { getValue(it) }.toMutableStateList()

    override fun put(key: K, value: Boolean): Boolean? =
        map.put(key, value)
            .also {
                if (value) {
                    enabledKeys.add(key)
                } else {
                    enabledKeys.remove(key)
                }
            }

    override fun toString(): String =
        "Map=$map | enabledKeys=${enabledKeys.toList()}"
}
