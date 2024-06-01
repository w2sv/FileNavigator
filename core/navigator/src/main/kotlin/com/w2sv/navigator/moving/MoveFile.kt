package com.w2sv.navigator.moving

import android.content.Context
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.domain.model.FileTypeKind
import com.w2sv.navigator.model.MediaStoreFile
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MoveFile(
    val mediaStoreFile: MediaStoreFile,
    val source: FileTypeKind.Source
) : Parcelable {

    constructor(
        mediaStoreFile: MediaStoreFile,
        fileType: FileTypeKind,
        sourceKind: FileTypeKind.Source.Kind
    ) : this(
        mediaStoreFile = mediaStoreFile,
        source = FileTypeKind.Source(fileType, sourceKind)
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