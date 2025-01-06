package util

import com.w2sv.common.util.MediaUri
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.observing.model.MediaStoreFileData

internal object TestInstance {

    val mediaStoreFileData = MediaStoreFileData(
        rowId = "1000012597",
        absPath = "primary/0/DCIM/Screenshots/somepicture.jpg",
        volumeRelativeDirPath = "DCIM/Screenshots",
        size = 7862183L,
        isPending = false,
        isTrashed = false
    )

    fun mediaStoreFileData(
        absPath: String,
        volumeRelativeDirPath: String,
        rowId: String = "1000012597",
        size: Long = 7862183L,
        isPending: Boolean = false,
        isTrashed: Boolean = false
    ): MediaStoreFileData =
        MediaStoreFileData(
            rowId = rowId,
            absPath = absPath,
            volumeRelativeDirPath = volumeRelativeDirPath,
            size = size,
            isPending = isPending,
            isTrashed = isTrashed
        )

    fun moveFile(
        mediaUri: MediaUri = MediaUri.parse("content://media/external/images/media/1000012597"),
        mediaStoreFileData: MediaStoreFileData = this.mediaStoreFileData,
        fileAndSourceType: FileAndSourceType = FileAndSourceType(
            fileType = FileType.Image,
            sourceType = SourceType.Screenshot
        )
    ): MoveFile =
        MoveFile(
            mediaUri = mediaUri,
            mediaStoreFileData = mediaStoreFileData,
            fileAndSourceType = fileAndSourceType
        )
}
