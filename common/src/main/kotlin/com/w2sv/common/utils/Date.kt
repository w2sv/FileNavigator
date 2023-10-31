package com.w2sv.common.utils

import java.util.Date
import kotlin.math.abs

fun Date.milliSecondsTo(other: Date): Long =
    abs(time - other.time)

fun Date.milliSecondsToNow(): Long =
    System.currentTimeMillis() - time
