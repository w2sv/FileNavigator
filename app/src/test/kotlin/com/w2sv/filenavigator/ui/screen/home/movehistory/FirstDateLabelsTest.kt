package com.w2sv.filenavigator.ui.screen.home.movehistory

import com.w2sv.core.common.R
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDate

class FirstDateLabelsTest {

    @Test
    fun `returns first index for each date representation`() {
        // Given
        val today = LocalDate.of(2026, 1, 12)
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val dates = listOf(
            today,
            today,
            yesterday,
            yesterday,
            twoDaysAgo,
            twoDaysAgo,
            twoDaysAgo
        )

        // When
        val result = firstDateLabels(
            dates = dates,
            getString = { resId ->
                when (resId) {
                    R.string.today -> "Today"
                    R.string.yesterday -> "Yesterday"
                    else -> error("Unexpected string id")
                }
            },
            today = today
        )

        // Then
        val expected = mapOf(
            0 to "Today",
            2 to "Yesterday",
            4 to "10.01.2026"
        )

        assertEquals(expected, result)
    }
}
