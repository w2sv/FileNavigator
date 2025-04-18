package com.w2sv.domain.model.filetype

import junit.framework.TestCase.assertEquals
import org.junit.Test

class PresetFileTypeTest {

    @Test
    fun testOrdinalsMap() {
        assertEquals(
            "{Image=0, Video=1, Audio=2, PDF=3, Text=4, Archive=5, APK=6, EBook=7}",
            PresetFileType.ordinalsMap.toString()
        )
    }

    @Test
    fun testGet() {
        assertEquals(PresetFileType.Image, PresetFileType[0])
        assertEquals(PresetFileType.EBook, PresetFileType[7])
    }

    @Test
    fun testExtensionSetToFileType() {
        assertEquals(
            PresetWrappingFileType.ExtensionSet(PresetFileType.Image, PresetFileType.Image.defaultColorInt),
            PresetFileType.Image.toFileType()
        )

        assertEquals(
            PresetWrappingFileType.ExtensionSet(PresetFileType.Image, 342347),
            PresetFileType.Image.toFileType(342347)
        )
    }

    @Test
    fun testExtensionConfigurableToFileType() {
        assertEquals(
            PresetWrappingFileType.ExtensionConfigurable(
                presetFileType = PresetFileType.Archive,
                colorInt = PresetFileType.Archive.defaultColorInt,
                excludedExtensions = emptySet()
            ),
            PresetFileType.Archive.toFileType()
        )

        assertEquals(
            PresetWrappingFileType.ExtensionConfigurable(
                presetFileType = PresetFileType.Archive,
                colorInt = 124325,
                excludedExtensions = setOf("sdfa")
            ),
            PresetFileType.Archive.toFileType(124325, setOf("sdfa"))
        )
    }
}
