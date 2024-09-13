package com.w2sv.common.utils

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class MediaUri(val uri: Uri) : Parcelable {

    fun documentUri(context: Context): DocumentUri? =
        MediaStore.getDocumentUri(context, uri)?.documentUri

    val id: MediaId?
        get() = MediaId.fromUri(uri)

    companion object {
        fun fromDocumentUri(context: Context, documentUri: DocumentUri): MediaUri? =
            MediaStore.getMediaUri(
                context,
                documentUri.uri
            )
                ?.mediaUri

        fun parse(uriString: String): MediaUri =
            Uri.parse(uriString).mediaUri
    }
}

val Uri.mediaUri: MediaUri
    get() = MediaUri(this)