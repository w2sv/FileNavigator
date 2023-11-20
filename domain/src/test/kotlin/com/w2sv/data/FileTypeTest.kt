package com.w2sv.data

import com.w2sv.domain.model.FileType
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileTypeTest {

    @Test
    fun testParceling() {
        com.w2sv.domain.model.FileType.Media.Image.testParceling()
        com.w2sv.domain.model.FileType.NonMedia.PDF.testParceling()
    }
}