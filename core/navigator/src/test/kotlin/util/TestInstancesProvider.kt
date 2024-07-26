package util

import com.w2sv.common.utils.MediaUri
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.observing.model.MediaStoreData
import java.time.LocalDateTime

internal object TestInstancesProvider {

    fun mediaStoreData(
        rowId: String = "9547834723",
        absPath: String = "primary/0/DCIM/Screenshots/somepicture.jpg",
        name: String = "somepicture.jpg",
        volumeRelativeDirPath: String = "DCIM/Screenshots",
        dateTimeAdded: LocalDateTime = LocalDateTime.now(),
        size: Long = 7862183L,
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

    fun moveFile(
        mediaUri: MediaUri = MediaUri.parse("content://media/external/images/media/1000012597"),
        mediaStoreData: MediaStoreData = mediaStoreData(),
        fileAndSourceType: FileAndSourceType = FileAndSourceType(
            FileType.Image,
            SourceType.Screenshot
        )
    ): MoveFile =
        MoveFile(
            mediaUri = mediaUri,
            mediaStoreData = mediaStoreData,
            fileAndSourceType = fileAndSourceType
        )
}