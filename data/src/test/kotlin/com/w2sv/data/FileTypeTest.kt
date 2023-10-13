package com.w2sv.data

import com.w2sv.data.model.FileType
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileTypeTest {

    @Test
    fun testParceling() {
        FileType.Media.Image.testParceling()
        FileType.NonMedia.PDF.testParceling()
    }
}