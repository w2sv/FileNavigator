package com.w2sv.navigator.model

import android.content.Context
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.domain.model.FileType
import kotlinx.parcelize.Parcelize

@Parcelize
data class MoveFile(
    val mediaStoreFile: MediaStoreFile,
    val source: FileType.Source
) : Parcelable {

    constructor(
        mediaStoreFile: MediaStoreFile,
        fileType: FileType,
        sourceKind: FileType.Source.Kind
    ) : this(
        mediaStoreFile = mediaStoreFile,
        source = FileType.Source(fileType, sourceKind)
    )

    fun getSimpleStorageMediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context,
            source.fileType.simpleStorageMediaType,
            mediaStoreFile.columnData.rowId
        )

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.MOVE_FILE"
    }
}