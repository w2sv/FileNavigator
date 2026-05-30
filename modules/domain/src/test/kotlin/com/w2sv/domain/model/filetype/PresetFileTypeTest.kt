package com.w2sv.domain.model.filetype

import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PresetFileTypeTest {

    @Test
    fun testOrdinals() {
        assertEquals(
            listOf("Image=0", "Video=1", "Audio=2", "PDF=3", "Text=4", "Archive=5", "APK=6", "EBook=7"),
            PresetFileType.entries.map { "${it.name}=${it.ordinal}" }
        )
    }

    @Test
    fun testGet() {
        assertEquals(PresetFileType.Image, PresetFileType[0])
        assertEquals(PresetFileType.EBook, PresetFileType[7])
    }

    @Test
    fun testFixedExtensionPresetToFileType() {
        assertEquals(
            FileType.preset(PresetFileType.Image),
            PresetFileType.Image.toFileType()
        )

        assertEquals(
            FileType.preset(PresetFileType.Image, 342347),
            PresetFileType.Image.toFileType(342347)
        )
    }

    @Test
    fun testConfigurableExtensionPresetToFileType() {
        assertEquals(
            FileType.preset(PresetFileType.Archive),
            PresetFileType.Archive.toFileType()
        )

        assertEquals(
            FileType.preset(PresetFileType.Archive, 124325, setOf("sdfa")),
            PresetFileType.Archive.toFileType(124325, setOf("sdfa"))
        )
    }

    @Test
    fun `fixed extension preset rejects excluded extensions`() {
        assertThrows(IllegalArgumentException::class.java) {
            PresetFileType.Image.toFileType(excludedExtensions = setOf("jpg"))
        }
    }
}
