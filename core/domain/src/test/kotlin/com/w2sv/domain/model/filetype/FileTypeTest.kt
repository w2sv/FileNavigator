package com.w2sv.domain.model.filetype

import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileTypeTest {

    @Test
    fun testParceling() {
        PresetFileType.Image.testParceling()
        PresetFileType.Video.testParceling()
        PresetFileType.Audio.testParceling()

        PresetFileType.EBook.testParceling()
        PresetFileType.PDF.testParceling()
        PresetFileType.APK.testParceling()
    }
}
