package com.w2sv.filenavigator.ui.util

import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

// TODO: Composed
fun <T : Any> snapshotStateListSaver() =
    listSaver<SnapshotStateList<T>, T>(
        save = { stateList -> stateList.toList() },
        restore = { it.toMutableStateList() }
    )
