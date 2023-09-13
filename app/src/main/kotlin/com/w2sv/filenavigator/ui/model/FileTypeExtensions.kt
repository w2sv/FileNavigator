package com.w2sv.filenavigator.ui.model

import androidx.compose.ui.graphics.Color
import com.w2sv.data.model.FileType

val FileType.color: Color get() = Color(colorLong)

fun MutableList<FileType>.sortByIsEnabledAndOriginalOrder(fileTypeStatuses: Map<FileType.Status.StoreEntry, FileType.Status>) {
    sortWith(
        compareByDescending<FileType> {
            fileTypeStatuses.getValue(
                it.status
            )
                .isEnabled
        }
            .thenBy(FileType.values::indexOf)
    )
}

/**
 * Assumes value corresponding to [key] to be one of [FileType.Status.Enabled] or [FileType.Status.Disabled].
 */
fun <K> MutableMap<K, FileType.Status>.toggle(key: K) {
    put(
        key,
        if (getValue(key) == FileType.Status.Disabled) FileType.Status.Enabled else FileType.Status.Disabled
    )
}