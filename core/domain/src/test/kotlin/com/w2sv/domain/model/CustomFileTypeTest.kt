package com.w2sv.domain.model

import com.w2sv.test.testParceling
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CustomFileTypeTest {

    @Test
    fun testParceling() {
        CustomFileType("Html", listOf("html"), 2134124, 1004).testParceling()
    }

    @Test
    fun testNewEmpty() {
        fun testOrdinal(existingOrdinals: List<Int>, expectedOrdinal: Int) {
            assertEquals(expectedOrdinal, CustomFileType.newEmpty(existingOrdinals.map { CustomFileType("", emptyList(), -1, it) }).ordinal)
        }

        testOrdinal(listOf(34, 0, 1, 3, 1003, 1007), 1008)
        testOrdinal(listOf(0, 1, 3), CustomFileType.MIN_ORDINAL)
    }
}
