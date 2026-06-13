package com.w2sv.core.util

fun String.containsSpecialCharacter(): Boolean =
    any { !it.isLetterOrDigit() && it != ' ' }
