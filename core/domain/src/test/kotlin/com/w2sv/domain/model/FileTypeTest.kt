package com.w2sv.domain.model

import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileTypeTest {

    @Test
    fun testParceling() {
        FileType.Image.testParceling()
        FileType.PDF.testParceling()
    }
}