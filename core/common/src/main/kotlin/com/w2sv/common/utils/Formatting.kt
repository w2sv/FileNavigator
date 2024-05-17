package com.w2sv.common.utils

fun String.removeSlashSuffix(): String =
    removeSuffix("/")

fun String.slashPrefixed(): String =
    "/$this"

fun String.colonSuffixed(): String =
    "$this:"

fun String.lineBreakSuffixed(): String =
    "$this\n"