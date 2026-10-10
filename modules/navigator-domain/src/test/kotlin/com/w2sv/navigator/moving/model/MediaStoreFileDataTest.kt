package com.w2sv.navigator.moving.model

import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.test.testParceling
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import util.TestInstance

@RunWith(RobolectricTestRunner::class)
internal class MediaStoreFileDataTest {

    @Test
    fun testName() {
        assertEquals("somepicture.jpg", TestInstance.mediaStoreEntry.fileName)
    }

    @Test
    fun testParcelling() {
        TestInstance.mediaStoreEntry.testParceling()
    }

    @Test
    fun testFileExtension() {
        assertEquals("jpg", TestInstance.mediaStoreEntry.fileExtension)
        assertEquals(
            "",
            TestInstance.mediaStoreFileData(
                absPath = "primary/0/DCIM",
                volumeRelativeDirPath = "DCIM/"
            )
                .fileExtension
        )
    }

    @Test
    fun testCompoundFileExtension() {
        assertEquals(
            "fb2.zip",
            TestInstance.mediaStoreFileData(
                absPath = "primary/0/Download/book.fb2.zip",
                volumeRelativeDirPath = "Download/"
            )
                .compoundFileExtension
        )
        assertEquals(
            "",
            TestInstance.mediaStoreFileData(
                absPath = "primary/0/Download/book.zip",
                volumeRelativeDirPath = "Download/"
            )
                .compoundFileExtension
        )
        assertEquals(
            "",
            TestInstance.mediaStoreFileData(
                absPath = "primary/0/Download/book.fb2",
                volumeRelativeDirPath = "Download/"
            )
                .compoundFileExtension
        )
    }

    @Test
    fun testParentDirName() {
        assertEquals("Screenshots", TestInstance.mediaStoreEntry.parentDirName)
    }

    @Test
    fun testSourceType() {
        assertEquals(SourceType.Screenshot, TestInstance.mediaStoreEntry.sourceType())
        assertEquals(
            SourceType.Camera,
            TestInstance.mediaStoreFileData(
                absPath = "primary/0/DCIM/somepicture.jpg",
                volumeRelativeDirPath = "DCIM/"
            )
                .sourceType()
        )
        assertEquals(
            SourceType.Recording,
            TestInstance.mediaStoreFileData(
                absPath = "primary/0/Recordings/record.mp3",
                volumeRelativeDirPath = "Recordings/"
            )
                .sourceType()
        )
        assertEquals(
            SourceType.OtherApp,
            TestInstance.mediaStoreFileData(
                absPath = "primary/0/Pictures/Wikipedia/picture.jpg",
                volumeRelativeDirPath = "Pictures/Wikipedia/"
            )
                .sourceType()
        )
    }
}
