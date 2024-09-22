package com.w2sv.navigator.moving.model

import android.content.Context
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.common.utils.MediaUri
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.observing.model.MediaStoreFileData
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MoveFile(
    val mediaUri: MediaUri,
    val mediaStoreFileData: MediaStoreFileData,
    val fileAndSourceType: FileAndSourceType
) : Parcelable {

    fun simpleStorageMediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context = context,
            mediaType = fileType.simpleStorageMediaType,
            id = mediaStoreFileData.rowId
        )

    val fileType: FileType
        get() = fileAndSourceType.fileType

    val sourceType: SourceType
        get() = fileAndSourceType.sourceType

    @IgnoredOnParcel
    val isGif: Boolean by lazy {
        fileType is FileType.Image && mediaStoreFileData.extension.lowercase() == "gif"
    }
}