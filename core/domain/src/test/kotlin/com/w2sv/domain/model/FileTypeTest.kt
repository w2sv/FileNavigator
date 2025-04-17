package com.w2sv.domain.model

import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileTypeTest {

    @Test
    fun testParceling() {
        StaticPresetFileType.Image.testParceling()
        StaticPresetFileType.Video.testParceling()
        StaticPresetFileType.Audio.testParceling()

        StaticPresetFileType.EBook.testParceling()
        StaticPresetFileType.PDF.testParceling()
        StaticPresetFileType.APK.testParceling()
    }
}
