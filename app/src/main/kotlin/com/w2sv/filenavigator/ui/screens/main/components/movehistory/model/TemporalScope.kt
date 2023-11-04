package com.w2sv.filenavigator.ui.screens.main.components.movehistory.model

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun determineTemporalScope(
    dateTime: LocalDateTime,
    now: LocalDateTime
): String = when {
    dateTime.toLocalDate() == now.toLocalDate() -> "Today"
    dateTime.toLocalDate() == now.minusDays(1).toLocalDate() -> "Yesterday"
    else -> dateTime.format(formatter.withZone(ZoneId.systemDefault()))
}

private val formatter = DateTimeFormatter.ofPattern("dd:MM:yyyy")