package com.w2sv.navigator.model

import android.content.Context
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.data.model.FileType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class NavigatableFile(
    val mediaStoreFile: MediaStoreFile,
    val type: FileType,
    val sourceKind: FileType.Source.Kind,
) : Parcelable {

    @IgnoredOnParcel
    val source: FileType.Source by lazy {
        FileType.Source(type, sourceKind)
    }

    fun getSimpleStorageMediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context,
            type.simpleStorageMediaType,
            mediaStoreFile.columnData.rowId
        )

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.MOVE_FILE"
    }
}