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
        MediaStore.getDocumentUri(context, uri)?.let { DocumentUri(it) }

    companion object {
        fun fromDocumentUri(context: Context, documentUri: DocumentUri): MediaUri? =
            MediaStore.getMediaUri(
                context,
                documentUri.uri
            )
                ?.let { MediaUri(it) }

        fun parse(uriString: String): MediaUri =
            MediaUri(Uri.parse(uriString))
    }
}