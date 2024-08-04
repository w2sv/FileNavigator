package com.w2sv.navigator.moving

import com.w2sv.domain.model.SourceType
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
        assertEquals("somepicture.jpg", TestInstance.mediaStoreFileDataDefault.name)
    }

    @Test
    fun testParcelling() {
        TestInstance.mediaStoreFileDataDefault.testParceling()
    }

    @Test
    fun testFileExtension() {
        assertEquals("jpg", TestInstance.mediaStoreFileDataDefault.extension)
        assertEquals(
            "",
            TestInstance.mediaStoreFileData(
                absPath = "primary/0/DCIM",
                volumeRelativeDirPath = "DCIM/"
            )
                .extension
        )
    }

    @Test
    fun testParentDirName() {
        assertEquals("Screenshots", TestInstance.mediaStoreFileDataDefault.parentDirName)
    }

    @Test
    fun testSourceType() {
        assertEquals(SourceType.Screenshot, TestInstance.mediaStoreFileDataDefault.sourceType())
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