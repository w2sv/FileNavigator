package com.w2sv.domain.model.filetype

import com.w2sv.test.testParceling
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PresetWrappingFileTypeTest {

    @Test
    fun `ExtensionSet parceling`() {
        PresetFileType.Image.toFileType().testParceling()
        PresetFileType.Image.toFileType(3245235).testParceling()
        PresetFileType.Video.toFileType(-1).testParceling()
        PresetFileType.APK.toFileType(-983264).testParceling()
    }

    @Test
    fun `ExtensionConfigurable parceling`() {
        PresetFileType.Text.toFileType().testParceling()
        PresetFileType.Text.toFileType(3253566, setOf("html", "json")).testParceling()
        PresetFileType.EBook.toFileType(-873624, setOf("dsaf", "kjs")).testParceling()
    }

    @Test
    fun `ExtensionConfigurable fileExtensions`() {
        fun test(
            expected: Set<String>,
            excludedExtensions: Set<String>,
            fileType: PresetFileType.ExtensionConfigurable
        ) {
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
}
