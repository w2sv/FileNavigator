package com.w2sv.navigator.domain.moving

import android.content.Context
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.anggrayudi.storage.media.MediaType
import com.w2sv.common.uri.MediaUri
import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import kotlinx.parcelize.Parcelize

@Parcelize
data class MoveFile(val mediaUri: MediaUri, val mediaStoreData: MediaStoreFileData, val fileAndSourceType: FileAndSourceType) :
    Parcelable {

    fun mediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context = context,
            mediaType = mediaType,
            id = mediaStoreData.rowId
        )

    val fileType: FileType
        get() = fileAndSourceType.fileType

    val sourceType: SourceType
        get() = fileAndSourceType.sourceType

    val mediaType: MediaType
        get() = fileType.mediaType

    val isGif: Boolean
        get() = fileType.wrappedPresetTypeOrNull is PresetFileType.Image && mediaStoreData.extension.lowercase() == "gif"
}
