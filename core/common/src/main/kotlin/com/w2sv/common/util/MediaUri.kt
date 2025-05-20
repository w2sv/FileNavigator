package com.w2sv.common.util

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.net.toUri
import kotlinx.parcelize.Parcelize
import slimber.log.e

@Parcelize
@JvmInline
value class MediaUri(val uri: Uri) : Parcelable {

    fun documentUri(context: Context): DocumentUri? =
        MediaStore.getDocumentUri(context, uri)?.documentUri

    fun id(): MediaId? =
        MediaId.fromUri(uri)

    companion object {
        /**
         * @throws java.lang.IllegalArgumentException java.io.FileNotFoundException: No item at content://media/6164-3862/file
         */
        fun fromDocumentUri(context: Context, documentUri: DocumentUri): MediaUri? =
            try {
                MediaStore.getMediaUri(
                    context,
                    documentUri.uri
                )
                    ?.mediaUri
            } catch (e: IllegalArgumentException) {
                e(e)
                null
            }

        fun parse(uriString: String): MediaUri =
            uriString.toUri().mediaUri
    }
}

val Uri.mediaUri: MediaUri
    get() = MediaUri(this)
