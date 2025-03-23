package com.w2sv.common.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class StringKtTest {

    @Test
    fun containsSpecialCharacter() {
        fun test(expected: Boolean, input: String) {
            assertEquals(expected, input.containsSpecialCharacter())
        }

        test(false, "")
        test(false, "sadfa")
        test(false, "sdafa6")
        test(false, "sfadfa sdafaxczv")
        test(true, ".")
        test(true, "sxczv.-cvx")
    }
}
