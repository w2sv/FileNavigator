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
        FileType.Video.testParceling()
        FileType.Audio.testParceling()

        FileType.EBook.testParceling()
        FileType.PDF.testParceling()
        FileType.APK.testParceling()
    }
}