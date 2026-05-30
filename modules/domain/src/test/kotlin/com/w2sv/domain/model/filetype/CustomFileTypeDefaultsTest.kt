package com.w2sv.domain.model.filetype

import com.w2sv.test.testParceling
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CustomFileTypeDefaultsTest {

    @Test
    fun parceling() {
        FileType.custom(
            name = "Html",
            fileExtensions = listOf("html"),
            colorInt = 2134124,
            ordinal = 1004
        )
            .testParceling()
    }

    @Test
    fun newEmptyCustom() {
        fun test(existingOrdinals: List<Int>, expectedOrdinal: Int) {
            assertEquals(
                expectedOrdinal,
                FileType.newEmptyCustom(
                    existingOrdinals.map {
                        FileType.custom(
                            name = "",
                            fileExtensions = emptyList(),
                            colorInt = -1,
                            ordinal = it
                        )
                    }
                ).ordinal
            )
        }

        test(listOf(34, 0, 1, 3, 1003, 1007), 1008)
        test(listOf(0, 1, 3), 1000)
    }
}
