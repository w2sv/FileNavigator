package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import com.w2sv.domain.model.PresetFileType
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.FileExtensionInvalidityReason.IsMediaFileTypeExtension
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FileTypeCreationDialogKtTest {

    @Test
    fun `test get IsMediaFileTypeExtension`() {
        fun test(expectedMediaFileType: PresetFileType.Media?, extension: String) {
            val expectedResult = expectedMediaFileType?.let { IsMediaFileTypeExtension(extension, it) }
            assertEquals(expectedResult, IsMediaFileTypeExtension.get(extension))
        }

        test(PresetFileType.Image, "jpg")
        test(PresetFileType.Audio, "mp3")
        test(null, "xml")
    }
}
