package util

import com.w2sv.common.utils.MediaUri
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.observing.model.MediaStoreData
import java.time.LocalDateTime

internal object TestInstance {

    val mediaStoreDataDefault = MediaStoreData(
        rowId = "1000012597",
        absPath = "primary/0/DCIM/Screenshots/somepicture.jpg",
        volumeRelativeDirPath = "DCIM/Screenshots",
        dateTimeAdded = LocalDateTime.now(),
        size = 7862183L,
        isPending = false,
        isTrashed = false
    )

    fun mediaStoreData(
        absPath: String,
        volumeRelativeDirPath: String,
        rowId: String = "1000012597",
        dateTimeAdded: LocalDateTime = LocalDateTime.now(),
        size: Long = 7862183L,
        isPending: Boolean = false,
        isTrashed: Boolean = false
    ): MediaStoreData =
        MediaStoreData(
            rowId = rowId,
            absPath = absPath,
            volumeRelativeDirPath = volumeRelativeDirPath,
            dateTimeAdded = dateTimeAdded,
            size = size,
            isPending = isPending,
            isTrashed = isTrashed
        )

    fun moveFile(
        mediaUri: MediaUri = MediaUri.parse("content://media/external/images/media/1000012597"),
        mediaStoreData: MediaStoreData = mediaStoreDataDefault,
        fileAndSourceType: FileAndSourceType = FileAndSourceType(
            fileType = FileType.Image,
            sourceType = SourceType.Screenshot
        )
    ): MoveFile =
        MoveFile(
            mediaUri = mediaUri,
            mediaStoreData = mediaStoreData,
            fileAndSourceType = fileAndSourceType
        )
}