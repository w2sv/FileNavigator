package com.w2sv.common.util

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.net.toUri
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class MediaUri(val uri: Uri) : Parcelable {

    fun documentUri(context: Context): DocumentUri? =
        MediaStore.getDocumentUri(context, uri)?.documentUri

    fun id(): MediaId? =
        MediaId.fromUri(uri)

    companion object {
        fun fromDocumentUri(context: Context, documentUri: DocumentUri): MediaUri? =
            MediaStore.getMediaUri(
                context,
                documentUri.uri
            )
                ?.mediaUri

        fun parse(uriString: String): MediaUri =
            uriString.toUri().mediaUri
    }
}

val Uri.mediaUri: MediaUri
    get() = MediaUri(this)
