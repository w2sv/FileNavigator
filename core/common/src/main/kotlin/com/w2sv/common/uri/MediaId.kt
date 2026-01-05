package com.w2sv.common.uri

import android.content.ContentUris
import android.net.Uri
import slimber.log.e

@JvmInline
value class MediaId(val value: Long) {

    companion object {
        fun parseFromUri(uri: Uri): MediaId? =
            try {
                val parsedId = ContentUris.parseId(uri)
                if (parsedId != -1L) {
                    MediaId(parsedId)
                } else {
                    null
                }
            } catch (e: Exception) {
                e { e.stackTraceToString() }
                null
            }
    }
}
