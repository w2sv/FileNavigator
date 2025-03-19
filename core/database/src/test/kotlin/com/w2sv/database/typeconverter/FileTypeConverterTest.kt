package com.w2sv.database.typeconverter

import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.PresetFileType
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class FileTypeConverterTest {

    @Test
    fun testBackAndForthPresetFileTypeConversion() {
        PresetFileType.values.forEach {
            assertEquals(it, FileTypeConverter.toFileType(FileTypeConverter.fromFileType(it)))
        }
    }

    @Test
    fun testBackAndForthCustomFileTypeConversion() {
        fun test(customFileType: CustomFileType) {
            val recreatedFileType = FileTypeConverter.toFileType(FileTypeConverter.fromFileType(customFileType)) as CustomFileType
            assertEquals(customFileType.name, recreatedFileType.name)
            assertEquals(customFileType.colorInt, recreatedFileType.colorInt)
        }

        test(CustomFileType("Html", emptyList(), 342523, -1))
    }
}
