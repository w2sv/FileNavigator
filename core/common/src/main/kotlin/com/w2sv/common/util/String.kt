package com.w2sv.common.util

fun String.containsSpecialCharacter(): Boolean =
    any { !it.isLetterOrDigit() && it != ' ' }

fun String.orNullIf(condition: (String) -> Boolean): String? =
    if (condition(this)) null else this
