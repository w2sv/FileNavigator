package com.w2sv.navigator.domain.moving

import android.content.Context
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.storage.uri.MediaUri
import kotlinx.parcelize.Parcelize

@Parcelize
data class NavigatableFile(val mediaUri: MediaUri, val mediaStoreEntry: MediaStoreEntry, val fileAndSourceType: FileAndSourceType) :
    Parcelable {

    fun mediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context = context,
            mediaType = mediaType,
            id = mediaStoreEntry.rowId
        )

    fun label(context: Context): String =
        fileAndSourceType.label(context, isGif)

    val fileType: FileType
        get() = fileAndSourceType.fileType

    val sourceType: SourceType
        get() = fileAndSourceType.sourceType

    val mediaType: MediaType
        get() = fileType.mediaType

    val isGif: Boolean
        get() = mediaStoreEntry.fileExtension.lowercase() == "gif"
}
