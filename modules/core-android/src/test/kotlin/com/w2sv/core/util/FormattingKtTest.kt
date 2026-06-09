package com.w2sv.core.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class FormattingKtTest {

    @Test
    fun formattedFileSize() {
        fun assert(expected: String, bytes: Long) {
            assertEquals(expected, formattedFileSize(bytes))
        }

        // Bytes
        assert("0 B", 0)
        assert("1 B", 1)
        assert("999 B", 999)

        // kB
        assert("1 kB", 1_000)
        assert("1 kB", 1_050)
        assert("1 kB", 1_230)
        assert("10 kB", 9_950)
        assert("10 kB", 10_000)
        assert("999 kB", 999_000)

        // kB → MB
        assert("1 MB", 999_500)
        assert("1 MB", 1_000_000)
        assert("1.23 MB", 1_230_000)
        assert("9.95 MB", 9_950_000)
        assert("10 MB", 10_000_000)

        // GB
        assert("1 GB", 1_000_000_000)
        assert("1.23 GB", 1_230_000_000)
        assert("9.95 GB", 9_950_000_000)

        // TB
        assert("1 TB", 1_000_000_000_000)
        assert("1.23 TB", 1_230_000_000_000)

        // PB
        assert("1 PB", 1_000_000_000_000_000)
        assert("1.23 PB", 1_230_000_000_000_000)
    }
}
