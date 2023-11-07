package com.w2sv.filenavigator.ui

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TemporalScopeTest {

    private val formatter = DateTimeFormatter.ofPattern("dd:MM:yyyy")

    @Test
    fun testDetermineTemporalScopeToday() {
        val now = LocalDateTime.of(2023, 1, 1, 12, 0)
        val dateTime = LocalDateTime.of(2023, 1, 1, 14, 0)
        val scope = determineTemporalScope(dateTime, now)

        assertEquals("Today", scope)
    }

    @Test
    fun testDetermineTemporalScopeYesterday() {
        val now = LocalDateTime.of(2023, 1, 1, 12, 0)
        val dateTime = LocalDateTime.of(2023, 1, 1, 2, 0)
        val scope = determineTemporalScope(dateTime, now)

        assertEquals("Yesterday", scope)
    }

    @Test
    fun testDetermineTemporalScopeOther() {
        val now = LocalDateTime.of(2023, 1, 1, 12, 0)
        val dateTime = LocalDateTime.of(2023, 1, 2, 12, 0)
        val scope = determineTemporalScope(dateTime, now)

        val expectedScope = dateTime.format(formatter.withZone(ZoneId.systemDefault()))
        assertEquals(expectedScope, scope)
    }
}