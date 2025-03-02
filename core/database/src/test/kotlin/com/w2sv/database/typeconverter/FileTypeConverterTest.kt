package com.w2sv.database.typeconverter

import com.w2sv.domain.model.PresetFileType
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class FileTypeConverterTest {

    @Test
    fun testBackAndForthFileTypeConversion() {
        PresetFileType.values.forEach {
            assertEquals(it, FileTypeConverter.toFileType(FileTypeConverter.fromFileType(it)))
        }
    }
}
