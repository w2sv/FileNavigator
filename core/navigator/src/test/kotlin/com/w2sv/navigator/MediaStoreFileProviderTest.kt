package com.w2sv.navigator

import com.w2sv.navigator.mediastore.contentHash
import com.w2sv.navigator.utils.getResourceFile
import com.w2sv.navigator.utils.sizeInMb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
class MediaStoreFileProviderTest {

    private val sha256MessageDigest = MessageDigest.getInstance("SHA-256")

    @Test
    fun imageContentHash() {
        val w2svImageContentHash = getResourceFile("Kyuss_Welcome_to_Sky_Valley.jpg")
            .also { println("Size: ${it.sizeInMb}mb") }
            .contentHash(sha256MessageDigest)
        val mandelbulbImageContentHash = getResourceFile("Mandelbulb.png")
            .also { println("Size: ${it.sizeInMb}mb") }
            .contentHash(sha256MessageDigest)

        assertEquals(
            w2svImageContentHash,
            getResourceFile("Kyuss_Welcome_to_Sky_Valley.jpg")
                .contentHash(sha256MessageDigest)
        )
        assertNotEquals(w2svImageContentHash, mandelbulbImageContentHash)
    }

    @Test
    fun emptyFilesContentHash() {
        val emptyTextHash = getResourceFile("empty.txt")
            .also { println("Size: ${it.sizeInMb}mb") }
            .contentHash(sha256MessageDigest)
            .also { println("Hash: $it") }

        val otherEmptyTextHash = getResourceFile("other_empty.txt")
            .also { println("Size: ${it.sizeInMb}mb") }
            .contentHash(sha256MessageDigest)
            .also { println("Hash: $it") }

        assertEquals(emptyTextHash, otherEmptyTextHash)
    }
}