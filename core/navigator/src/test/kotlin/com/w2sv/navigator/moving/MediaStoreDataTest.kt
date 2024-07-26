package com.w2sv.navigator.moving

import com.w2sv.domain.model.SourceType
import com.w2sv.test.testParceling
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import util.TestInstance

@RunWith(RobolectricTestRunner::class)
internal class MediaStoreDataTest {

    @Test
    fun testName() {
        assertEquals("somepicture.jpg", TestInstance.mediaStoreDataDefault.name)
    }

    @Test
    fun testParcelling() {
        TestInstance.mediaStoreDataDefault.testParceling()
    }

    @Test
    fun testFileExtension() {
        assertEquals("jpg", TestInstance.mediaStoreDataDefault.extension)
    }

    @Test
    fun testContainingDirName() {
        assertEquals("Screenshots", TestInstance.mediaStoreDataDefault.containingDirName)
    }

    @Test
    fun testSourceType() {
        assertEquals(SourceType.Screenshot, TestInstance.mediaStoreDataDefault.sourceType())
        assertEquals(
            SourceType.Camera,
            TestInstance.mediaStoreData(
                absPath = "primary/0/DCIM/somepicture.jpg",
                volumeRelativeDirPath = "DCIM/"
            )
                .sourceType()
        )
        assertEquals(
            SourceType.Recording,
            TestInstance.mediaStoreData(
                absPath = "primary/0/Recordings/record.mp3",
                volumeRelativeDirPath = "Recordings/"
            )
                .sourceType()
        )
        assertEquals(
            SourceType.OtherApp,
            TestInstance.mediaStoreData(
                absPath = "primary/0/Pictures/Wikipedia/picture.jpg",
                volumeRelativeDirPath = "Pictures/Wikipedia/"
            )
                .sourceType()
        )
    }

//    @Test
//    fun testNonIncrementedNameWOExtension() {
//        val instance = TestInstancesProvider.mediaStoreData(name = "someName.jpeg")
//        assertEquals("someName", instance.nonIncrementedNameWOExtension)
//
//        val instance1 = TestInstancesProvider.mediaStoreData(name = "someName(1).jpeg")
//        assertEquals("someName", instance1.nonIncrementedNameWOExtension)
//
//        val instance2 = TestInstancesProvider.mediaStoreData(name = "someName (435).jpeg")
//        assertEquals("someName ", instance2.nonIncrementedNameWOExtension)
//    }
}