package com.w2sv.filenavigator.ui.screens.home.components.movehistory.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class IndexToDateRepresentationMapKtTest {

    @Test
    fun testGetDateRepresentationToday() {
        val now = LocalDateTime.now()
        val result = getDateRepresentation(now, now)
        assertEquals("Today", result)
    }

    @Test
    fun testGetDateRepresentationYesterday() {
        val now = LocalDateTime.now()
        val dateTime = now.minusDays(1)
        val result = getDateRepresentation(dateTime, now)
        assertEquals("Yesterday", result)
    }

    @Test
    fun testGetDateRepresentationOtherDate() {
        val now = LocalDateTime.now()
        val dateTime = now.minusDays(2)
        val result = getDateRepresentation(dateTime, now)
        val expected = dateTime.format(
            DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault())
        )
        assertEquals(expected, result)
    }
}