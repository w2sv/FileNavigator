package com.w2sv.common.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import androidx.annotation.IntRange

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

fun Cursor.getBoolean(@IntRange(from = 0) i: Int): Boolean =
    when (getString(i)) {
        "0" -> false
        "1" -> true
        else -> throw IllegalStateException("Invalid string for boolean conversion")
    }