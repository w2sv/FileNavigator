package com.w2sv.navigator

import com.w2sv.navigator.utils.TestInstancesProvider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class MediaStoreDataTest {

    @Test
    fun testExistsForMoreThan() {
        val instance =
            TestInstancesProvider.getMediaStoreColumnData(
                dateTimeAdded = LocalDateTime.ofInstant(
                    Instant.now().minusSeconds(10),
                    ZoneId.systemDefault()
                )
            )

        assertFalse(instance.addedBeforeForMoreThan(15_000))
        assertTrue(instance.addedBeforeForMoreThan(10_000))
        assertTrue(instance.addedBeforeForMoreThan(1_000))
    }

    @Test
    fun testFileExtension() {
        val instance = TestInstancesProvider.getMediaStoreColumnData(name = "someName.jpeg")

        assertEquals("jpeg", instance.fileExtension)
    }

    @Test
    fun testNonIncrementedNameWOExtension() {
        val instance = TestInstancesProvider.getMediaStoreColumnData(name = "someName.jpeg")
        assertEquals("someName", instance.nonIncrementedNameWOExtension)

        val instance1 = TestInstancesProvider.getMediaStoreColumnData(name = "someName(1).jpeg")
        assertEquals("someName", instance1.nonIncrementedNameWOExtension)

        val instance2 = TestInstancesProvider.getMediaStoreColumnData(name = "someName (435).jpeg")
        assertEquals("someName ", instance2.nonIncrementedNameWOExtension)
    }
}