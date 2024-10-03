package com.w2sv.database.typeconverter

import com.w2sv.domain.model.FileType
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class FileTypeConverterTest {

    @Test
    fun testBackAndForthFileTypeConversion() {
        FileType.values.forEach {
            assertEquals(it, FileTypeConverter.toFileType(FileTypeConverter.fromFileType(it)))
        }
    }
}