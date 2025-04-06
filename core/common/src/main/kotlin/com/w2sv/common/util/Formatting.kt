package com.w2sv.common.util

import java.text.NumberFormat
import java.util.Locale

fun String.removeSlashSuffix(): String =
    removeSuffix("/")

fun String.slashPrefixed(): String =
    "/$this"

fun String.lineBreakSuffixed(): String =
    "$this\n"

fun String.colonSuffixed(): String =
    "$this:"

fun formattedFileSize(bytes: Long, locale: Locale = Locale.getDefault()): String {
    if (bytes in -999..999) {
        return "$bytes B"
    }
    val dimensionPrefixIterator = "kMGTPE".iterator()
    var dimensionPrefix = dimensionPrefixIterator.next()
    var byteCount = bytes.toDouble()
    while (byteCount <= -999_950 || byteCount >= 999_950) {
        byteCount /= 1000
        dimensionPrefix = dimensionPrefixIterator.next()
    }
    val numberFormat = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = 3
    }
    return "${numberFormat.format(byteCount / 1000)} ${dimensionPrefix}B"
}
