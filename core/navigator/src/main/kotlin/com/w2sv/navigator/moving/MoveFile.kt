package com.w2sv.navigator.moving

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.model.MediaStoreFile
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MoveFile(
    val mediaStoreFile: MediaStoreFile,
    val fileAndSourceType: FileAndSourceType,
    val autoMoveDestination: Uri?
) : Parcelable {

    fun getSimpleStorageMediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context,
            fileAndSourceType.fileType.simpleStorageMediaType,
            mediaStoreFile.columnData.rowId
        )

    val fileType: FileType
        get() = fileAndSourceType.fileType

    val sourceType: SourceType
        get() = fileAndSourceType.sourceType

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.MOVE_FILE"
    }
}