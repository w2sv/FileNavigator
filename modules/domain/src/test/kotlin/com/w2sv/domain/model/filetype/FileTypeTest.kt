package com.w2sv.domain.model.filetype

import com.w2sv.test.testParceling
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileTypeTest {

    @Test
    fun `fixed extension preset parceling`() {
        (PresetFileType.Image.toFileType() as FileType.FixedPreset).testParceling()
        (PresetFileType.Image.toFileType(3245235) as FileType.FixedPreset).testParceling()
        (PresetFileType.Video.toFileType(-1) as FileType.FixedPreset).testParceling()
    }

    @Test
    fun `configurable extension preset parceling`() {
        (PresetFileType.Text.toFileType() as FileType.ConfigurablePreset).testParceling()
        (PresetFileType.Text.toFileType(3253566, setOf("html", "json")) as FileType.ConfigurablePreset).testParceling()
        (PresetFileType.APK.toFileType(-983264, setOf("xapk")) as FileType.ConfigurablePreset).testParceling()
        (PresetFileType.EBook.toFileType(-873624, setOf("dsaf", "kjs")) as FileType.ConfigurablePreset).testParceling()
    }

    @Test
    fun `configurable extension preset fileExtensions`() {
        fun test(expected: Set<String>, excludedExtensions: Set<String>, fileType: PresetFileType) {
            assertEquals(
                expected,
                fileType.toFileType(excludedExtensions = excludedExtensions).fileExtensions
            )
        }

        test(
            expected = setOf("txt"),
            excludedExtensions = setOf(
                "text", "asc", "csv", "xml", "json", "md", "doc", "docx", "odt",
                "wpd", "cfg", "log", "ini", "properties", "html"
            ),
            fileType = PresetFileType.Text
        )

        test(
            expected = setOf(
                "text", "asc", "csv", "xml", "json", "md", "doc", "docx", "odt",
                "wpd", "cfg", "log", "ini", "properties", "html"
            ),
            excludedExtensions = setOf("txt"),
            fileType = PresetFileType.Text
        )
    }

    @Test
    fun `preset and custom file types use distinct model variants`() {
        assertTrue(PresetFileType.Image.toFileType() is FileType.FixedPreset)
        assertTrue(PresetFileType.Text.toFileType() is FileType.ConfigurablePreset)
    }
}
