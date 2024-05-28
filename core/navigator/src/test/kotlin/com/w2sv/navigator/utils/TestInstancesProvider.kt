package com.w2sv.navigator.utils

import android.net.Uri
import com.w2sv.navigator.model.MediaStoreColumnData
import com.w2sv.navigator.model.MediaStoreFile
import java.time.LocalDateTime

internal object TestInstancesProvider {

    fun getMediaStoreColumnData(
        rowId: String = "",
        absPath: String = "",
        name: String = "",
        volumeRelativeDirPath: String = "",
        dateTimeAdded: LocalDateTime = LocalDateTime.now(),
        size: Long = 0L,
        isPending: Boolean = false,
        isTrashed: Boolean = false
    ): MediaStoreColumnData =
        MediaStoreColumnData(
            rowId = rowId,
            absPath = absPath,
            volumeRelativeDirPath = volumeRelativeDirPath,
            name = name,
            dateTimeAdded = dateTimeAdded,
            size = size,
            isPending = isPending,
            isTrashed = isTrashed
        )

    fun getMediaStoreFile(): MediaStoreFile =
        MediaStoreFile(
            Uri.parse("content://media/external/images/media/1000012597"),
            getMediaStoreColumnData(),
            "c9232c60753d1c181e8bb8b9658c43f2563bbc649dee793659b5df60ca0e57a0"
        )
}