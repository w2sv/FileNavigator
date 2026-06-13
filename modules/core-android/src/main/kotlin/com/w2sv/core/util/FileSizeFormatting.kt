package com.w2sv.core.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats a byte count (as returned by [java.io.File.length]) into a human-readable file size.
 *
 * Uses decimal (1000-based) units (kB, MB, GB, …), rounds to at most two fractional
 * digits, and applies locale-specific number formatting.
 */
fun formattedFileSize(byteCount: Long, locale: Locale = Locale.getDefault()): String {
    if (byteCount < 1000) {
        return "$byteCount B"
    }

    val units = arrayOf("kB", "MB", "GB", "TB", "PB", "EB")
    var value = byteCount.toDouble()
    var unitIndex = -1

    while (value >= 999.5 && unitIndex < units.lastIndex) {
        value /= 1000.0
        unitIndex++
    }

    val format = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = if (unitIndex == 0) 0 else 2 // Don't display fraction digits on kB
        minimumFractionDigits = 0
    }

    return "${format.format(value)} ${units[unitIndex]}"
}
