package com.w2sv.database.typeconverter

import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetFileType
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class FileTypeConverterTest {

    @Test
    fun testBackAndForthPresetFileTypeConversion() {
        PresetFileType.entries.forEach {
            assertEquals(
                it,
                it.toFileType().backAndForthConverted().presetTypeOrNull
            )
        }
    }

    @Test
    fun testBackAndForthCustomFileTypeConversion() {
        val customFileType = FileType.custom("Html", emptyList(), 342523, 1006)
        val recreatedFileType = customFileType.backAndForthConverted()

        assertEquals(customFileType.name, (recreatedFileType as FileType.Custom).name)
        assertEquals(customFileType.colorInt, recreatedFileType.colorInt)
        assertEquals(customFileType.ordinal, recreatedFileType.ordinal)
    }
}

private fun FileType.backAndForthConverted(): FileType =
    FileTypeConverter.toFileType(FileTypeConverter.fromFileType(this))
