package com.w2sv.common.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class LocalDateTimeUtilsTest {

    @Test
    fun testMilliSecondsTo() {
        val dateTime1 = LocalDateTime.of(2023, 1, 1, 12, 0)
        val dateTime2 = LocalDateTime.of(2023, 1, 1, 12, 0, 5)
        val difference = dateTime1.milliSecondsTo(dateTime2)

        assertEquals(5000, difference)
    }

    @Test
    fun testLocalDateTimeFromUnixTimeStamp() {
        val unixTimestamp = 1699041284L
        val expectedDateTime = LocalDateTime.of(2023, 11, 3, 20, 54, 44)
        val dateTime = localDateTimeFromUnixTimeStamp(unixTimestamp)

        assertEquals(expectedDateTime, dateTime)
    }
}