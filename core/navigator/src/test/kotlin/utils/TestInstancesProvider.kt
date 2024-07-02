package utils

import com.w2sv.common.utils.MediaUri
import com.w2sv.navigator.mediastore.MediaStoreData
import com.w2sv.navigator.mediastore.MoveFile
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
    ): MediaStoreData =
        MediaStoreData(
            rowId = rowId,
            absPath = absPath,
            volumeRelativeDirPath = volumeRelativeDirPath,
            name = name,
            dateTimeAdded = dateTimeAdded,
            size = size,
            isPending = isPending,
            isTrashed = isTrashed
        )

    fun getMediaStoreFile(): MoveFile =
        MoveFile(
            MediaUri.parse("content://media/external/images/media/1000012597"),
            getMediaStoreColumnData(),
            "c9232c60753d1c181e8bb8b9658c43f2563bbc649dee793659b5df60ca0e57a0"
        )
}