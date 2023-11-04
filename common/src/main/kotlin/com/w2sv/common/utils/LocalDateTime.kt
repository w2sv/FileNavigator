package com.w2sv.common.utils

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.milliSecondsTo(other: LocalDateTime): Long =
    Duration.between(this, other).toMillis()

fun LocalDateTime.milliSecondsToNow(): Long =
    milliSecondsTo(LocalDateTime.now())

fun localDateTimeFromUnixTimeStamp(secondTimestamp: Long): LocalDateTime =
    LocalDateTime.ofInstant(
        Instant.ofEpochSecond(secondTimestamp),
        ZoneId.systemDefault()
    )