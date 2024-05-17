package com.w2sv.common.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri

fun <R> ContentResolver.query(
    uri: Uri,
    columns: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    onCursor: (Cursor) -> R
): R? =
    query(
        uri,
        columns,
        selection,
        selectionArgs,
        null
    )
        ?.use {
            it.moveToFirst()
            onCursor(it)
        }