package com.w2sv.navigator.utils

import android.net.Uri
import com.w2sv.navigator.model.MediaStoreColumnData
import com.w2sv.navigator.model.MediaStoreFile
import java.util.Date

object TestInstancesProvider {

    fun getMediaStoreColumnData(
        rowId: String = "",
        absPath: String = "",
        name: String = "",
        volumeRelativeDirPath: String = "",
        dateAdded: Date = Date(),
        size: Long = 0L,
        isPending: Boolean = false
    ): MediaStoreColumnData =
        MediaStoreColumnData(
            rowId = rowId,
            absPath = absPath,
            volumeRelativeDirPath = volumeRelativeDirPath,
            name = name,
            dateAdded = dateAdded,
            size = size,
            isPending = isPending,
        )

    fun getMediaStoreFile(): MediaStoreFile =
        MediaStoreFile(
            Uri.parse("content://example-uri"),
            getMediaStoreColumnData(),
            "abc123"
        )
}