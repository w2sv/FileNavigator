package com.w2sv.common.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ListKtTest {

    @Test
    fun replaceLast() {
        assertEquals(listOf(1, 2, 8), listOf(1, 2, 3).replaceLast(8))
        assertEquals(emptyList<Int>(), emptyList<Int>().replaceLast(8))
    }
}
