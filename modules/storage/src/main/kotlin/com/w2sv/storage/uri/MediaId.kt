package com.w2sv.storage.uri

import android.content.ContentUris
import android.net.Uri
import slimber.log.e

@JvmInline
value class MediaId(val value: Long) {

    companion object {
        /**
         * @see ContentUris.parseId
         */
        fun parseFromUri(uri: Uri): MediaId? =
            try {
                val parsedId = ContentUris.parseId(uri)
                parsedId.takeIf { it != -1L }?.run(::MediaId)
            } catch (e: Exception) {
                e { e.stackTraceToString() }
                null
            }
    }
}
