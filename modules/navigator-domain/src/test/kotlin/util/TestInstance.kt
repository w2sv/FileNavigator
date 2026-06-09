package util

import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.navigator.domain.moving.MediaStoreEntry
import com.w2sv.navigator.domain.moving.NavigatableFile
import com.w2sv.storage.uri.MediaUri

internal object TestInstance {

    val mediaStoreEntry = MediaStoreEntry(
        rowId = "1000012597",
        absPath = "primary/0/DCIM/Screenshots/somepicture.jpg",
        relativePath = "DCIM/Screenshots",
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
    ): MediaStoreEntry =
        MediaStoreEntry(
            rowId = rowId,
            absPath = absPath,
            relativePath = volumeRelativeDirPath,
            size = size,
            isPending = isPending,
            isTrashed = isTrashed
        )

    fun moveFile(
        mediaUri: MediaUri = MediaUri.parse("content://media/external/images/media/1000012597"),
        mediaStoreEntry: MediaStoreEntry = this.mediaStoreEntry,
        fileAndSourceType: FileAndSourceType = FileAndSourceType(
            fileType = PresetFileType.Image.toFileType(),
            sourceType = SourceType.Screenshot
        )
    ): NavigatableFile =
        NavigatableFile(
            mediaUri = mediaUri,
            mediaStoreEntry = mediaStoreEntry,
            fileAndSourceType = fileAndSourceType
        )
}
