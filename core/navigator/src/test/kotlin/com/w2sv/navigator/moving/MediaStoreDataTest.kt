package com.w2sv.navigator.moving

import com.w2sv.test.testParceling
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import util.TestInstancesProvider

@RunWith(RobolectricTestRunner::class)
internal class MediaStoreDataTest {

    @Test
    fun testParcelling() {
        TestInstancesProvider.mediaStoreData().testParceling()
    }

    @Test
    fun testFileExtension() {
        val instance = TestInstancesProvider.mediaStoreData(name = "someName.jpeg")

        assertEquals("jpeg", instance.fileExtension)
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