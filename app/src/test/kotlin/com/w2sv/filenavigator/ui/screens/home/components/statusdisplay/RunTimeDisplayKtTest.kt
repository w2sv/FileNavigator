package com.w2sv.filenavigator.ui.screens.home.components.statusdisplay

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class RunTimeDisplayKtTest {

    @Test
    fun testGetDayUntilSecondsRepresentation() {
        // Test regular conversion
        assertEquals(
            "01:02:30:15",
            Duration.ofDays(1).plusHours(2).plusMinutes(30).plusSeconds(15)
                .getDayUntilSecondsRepresentation()
        )

        // Test removal of leading '00:'
        assertEquals(
            "01:02:03",
            Duration.ofHours(1).plusMinutes(2).plusSeconds(3).getDayUntilSecondsRepresentation()
        )

        // Test correct surplus conversion
        assertEquals(
            "02:09:31:27",
            Duration.ofHours(56).plusMinutes(75).plusSeconds(987).getDayUntilSecondsRepresentation()
        )
    }
}