package com.w2sv.common.util

import android.content.ContentUris
import android.net.Uri
import slimber.log.e

@JvmInline
value class MediaId(val value: Long) {

    companion object {
        fun fromUri(uri: Uri): MediaId? {
            return try {
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
}
