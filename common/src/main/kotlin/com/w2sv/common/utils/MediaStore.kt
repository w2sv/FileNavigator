package com.w2sv.common.utils

import android.content.ContentResolver
import android.net.Uri

/**
 * @see
 *      https://stackoverflow.com/a/16511111/12083276
 */
fun ContentResolver.queryMediaStoreData(
    uri: Uri,
    columns: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null
): List<String?>? =
    query(
        uri,
        columns,
        selection,
        selectionArgs,
        null
    )?.run {
        moveToFirst()
        columns.map { getString(getColumnIndexOrThrow(it)) }
            .also { close() }
    }

fun ContentResolver.queryNonNullMediaStoreData(
    uri: Uri,
    columns: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null
): List<String>? =
    query(
        uri,
        columns,
        selection,
        selectionArgs,
        null
    )?.run {
        moveToFirst()
        columns
            .map { columnIndex ->
                getString(getColumnIndexOrThrow(columnIndex))
                    .also { if (it == null) return@run null }
            }
            .also { close() }
    }

fun parseBoolean(mediaStoreString: String): Boolean =
    when (mediaStoreString) {
        "0" -> false
        "1" -> true
        else -> throw IllegalStateException()
    }