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
            Uri.parse("content://media/external/images/media/1000012597"),
            getMediaStoreColumnData(),
            "c9232c60753d1c181e8bb8b9658c43f2563bbc649dee793659b5df60ca0e57a0"
        )
}