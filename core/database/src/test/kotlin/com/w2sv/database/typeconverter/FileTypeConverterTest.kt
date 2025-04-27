package com.w2sv.database.typeconverter

import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetFileType
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class FileTypeConverterTest {

    @Test
    fun testBackAndForthPresetFileTypeConversion() {
        PresetFileType.values.forEach {
            assertEquals(
                it,
                it.toDefaultFileType().backAndForthConverted().wrappedPresetTypeOrNull
            )
        }
    }

    @Test
    fun testBackAndForthCustomFileTypeConversion() {
        val customFileType = CustomFileType("Html", emptyList(), 342523, 1006)
        val recreatedFileType = customFileType.backAndForthConverted() as CustomFileType

        assertEquals(customFileType.name, recreatedFileType.name)
        assertEquals(customFileType.colorInt, recreatedFileType.colorInt)
        assertEquals(customFileType.ordinal, recreatedFileType.ordinal)
    }
}

private fun FileType.backAndForthConverted(): FileType =
    FileTypeConverter.toFileType(FileTypeConverter.fromFileType(this))
