package com.w2sv.navigator.model

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.data.model.FileType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * @param uri The MediaStore URI.
 */
@Parcelize
data class MoveFile(
    val uri: Uri,
    val type: FileType,
    val sourceKind: FileType.Source.Kind,
    val data: MediaStoreData
) : Parcelable {

    @IgnoredOnParcel
    val source: FileType.Source by lazy {
        FileType.Source(type, sourceKind)
    }

    fun getMediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context,
            type.mediaType,
            data.id
        )

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.MOVE_FILE"
    }
}