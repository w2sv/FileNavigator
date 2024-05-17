package com.w2sv.common.utils

import android.database.Cursor
import androidx.annotation.IntRange

fun Cursor.getStringOrThrow(columnName: String): String =
    getString(getColumnIndexOrThrow(columnName))

fun Cursor.getLongOrThrow(columnName: String): Long =
    getLong(getColumnIndexOrThrow(columnName))

fun Cursor.getBooleanOrThrow(columnName: String): Boolean =
    getBooleanOrThrow(getColumnIndexOrThrow(columnName))

fun Cursor.getBooleanOrThrow(@IntRange(from = 0) i: Int): Boolean =
    when (getString(i)) {
        "0" -> false
        "1" -> true
        else -> throw IllegalStateException("Invalid string for boolean conversion")
    }
