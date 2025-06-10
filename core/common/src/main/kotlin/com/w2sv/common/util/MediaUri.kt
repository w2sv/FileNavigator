package com.w2sv.common.util

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.net.toUri
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.anggrayudi.storage.media.MediaType
import kotlinx.parcelize.Parcelize
import slimber.log.e

@Parcelize
@JvmInline
value class MediaUri(val uri: Uri) : Parcelable {

    fun documentUri(context: Context): DocumentUri? =
        try {
            MediaStore.getDocumentUri(context, uri)?.documentUri
        } catch (e: IllegalArgumentException) {
            e { "Encountered ${e.message} whilst attempting to convert MediaUri to DocumentUri" }
            null
        }

    fun id(): MediaId? =
        MediaId.parseFromUri(uri)

    fun idIncremented(): MediaUri? =
        id()?.let { nonNullId -> parse("${uri.toString().substringBeforeLast("/")}/${nonNullId.value + 1}") }

    fun mediaFile(mediaType: MediaType, context: Context): MediaFile? =
        id()?.let {
            MediaStoreCompat.fromMediaId(
                context = context,
                mediaType = mediaType,
                id = it.value
            )
        }

    companion object {
        /**
         * @throws java.lang.IllegalArgumentException java.io.FileNotFoundException: No item at content://media/6164-3862/file
         * @throws java.lang.SecurityException: MediaProvider: User 11009 does not have read permission on ...
         */
        fun fromDocumentUri(context: Context, documentUri: DocumentUri): MediaUri? =
            try {
                MediaStore.getMediaUri(
                    context,
                    documentUri.uri
                )
                    ?.mediaUri
            } catch (e: Exception) {
                e { "Encountered ${e::class.java.name}: ${e.message} whilst attempting to get media Uri" }
                null
            }

        fun parse(uriString: String): MediaUri =
            uriString.toUri().mediaUri
    }
}

val Uri.mediaUri: MediaUri
    get() = MediaUri(this)
