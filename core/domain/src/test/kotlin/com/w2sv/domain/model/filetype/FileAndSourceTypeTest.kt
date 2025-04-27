package com.w2sv.domain.model.filetype

import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileAndSourceTypeTest {

    @Test
    fun `test parcelling`() {
        FileAndSourceType(PresetFileType.Image.toFileType(), SourceType.Screenshot).testParceling()
        FileAndSourceType(PresetFileType.Image.toFileType(color = 78325), SourceType.Screenshot).testParceling()
        FileAndSourceType(PresetFileType.EBook.toFileType(color = 234453, setOf("sdasf", "xscvs")), SourceType.Download).testParceling()
        FileAndSourceType(CustomFileType("Custom", listOf("ext"), 2345213, 1008), SourceType.Download).testParceling()
    }
}
