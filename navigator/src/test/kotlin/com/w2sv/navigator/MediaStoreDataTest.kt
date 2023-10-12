package com.w2sv.navigator

import com.w2sv.navigator.model.MediaStoreData
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.Date

class MediaStoreDataTest {

    @Test
    fun testExistsForMoreThan() {
        val instance = getInstance(dateAdded = Date.from(Instant.now().minusSeconds(10)))

        assertFalse(instance.addedBeforeForMoreThan(15_000))
        assertTrue(instance.addedBeforeForMoreThan(10_000))
        assertTrue(instance.addedBeforeForMoreThan(1_000))
    }

    @Test
    fun testFileExtension() {
        val instance = getInstance(name = "someName.jpeg")

        assertEquals("jpeg", instance.fileExtension)
    }

    @Test
    fun testNonIncrementedNameWOExtension() {
        val instance = getInstance(name = "someName.jpeg")
        assertEquals("someName", instance.nonIncrementedNameWOExtension)

        val instance1 = getInstance(name = "someName(1).jpeg")
        assertEquals("someName", instance1.nonIncrementedNameWOExtension)

        val instance2 = getInstance(name = "someName (435).jpeg")
        assertEquals("someName ", instance2.nonIncrementedNameWOExtension)
    }

    companion object {
        private fun getInstance(
            rowId: String = "",
            absPath: String = "",
            name: String = "",
            volumeRelativeDirPath: String = "",
            dateAdded: Date = Date(),
            size: Long = 0L,
            isDownload: Boolean = false,
            isPending: Boolean = false
        ): MediaStoreData =
            MediaStoreData(
                rowId = rowId,
                absPath = absPath,
                volumeRelativeDirPath = volumeRelativeDirPath,
                name = name,
                dateAdded = dateAdded,
                size = size,
                isDownload = isDownload,
                isPending = isPending,
                sha256 = ""
            )
    }
}