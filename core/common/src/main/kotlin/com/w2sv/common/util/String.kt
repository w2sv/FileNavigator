package com.w2sv.common.util

fun String.containsSpecialCharacter(): Boolean =
    any { !it.isLetterOrDigit() && it != ' ' }
