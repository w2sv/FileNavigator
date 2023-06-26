package com.w2sv.filenavigator.utils

fun Iterable<Boolean>.allFalseAfterEnteringValue(newValue: Boolean): Boolean =
    !newValue && count { it } <= 1