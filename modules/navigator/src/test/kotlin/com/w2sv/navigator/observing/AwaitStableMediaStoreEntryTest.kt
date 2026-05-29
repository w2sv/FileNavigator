package com.w2sv.navigator.observing

import com.w2sv.navigator.domain.moving.MediaStoreEntry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AwaitStableMediaStoreEntryTest {

    @Test
    fun `returns null when provider returns null`() =
        runTest {
            val result = awaitStableMediaStoreEntry(provideEntry = { null })
            assertNull(result)
        }

    @Test
    fun `is cancellable while waiting for stability`() =
        runTest {
            val provider = { mediaStoreEntry(100) }

            val job = launch {
                awaitStableMediaStoreEntry(
                    provideEntry = provider,
                    stableWindowMs = 5_000
                )
            }

            advanceTimeBy(400)
            job.cancel()

            assertTrue(job.isCancelled)
        }

    @Test
    fun `returns data when file size is stable for the required window`() =
        runTest {
            var calls = 0
            val provider = {
                calls++
                mediaStoreEntry(size = 100)
            }

            var fakeTime = 0L
            val resultDeferred = async {
                awaitStableMediaStoreEntry(
                    provideEntry = provider,
                    elapsedTime = { fakeTime },
                    stableWindowMs = 1_000
                )
            }

            // Simulate time passing with file size unchanged
            repeat(6) {
                advanceTimeBy(200) // advances the virtual time
                fakeTime += 200
            }

            val result = resultDeferred.await()
            assertEquals(100L, result?.size)
            assertEquals(6, calls)
        }

    @Test
    fun `does not complete while file size keeps changing`() =
        runTest {
            val sizes = listOf(10L, 20L, 30L, 40L).iterator()
            val provider = { if (sizes.hasNext()) mediaStoreEntry(sizes.next()) else mediaStoreEntry(40) }

            var fakeTime = 0L
            val deferred = async {
                awaitStableMediaStoreEntry(
                    provideEntry = provider,
                    elapsedTime = { fakeTime },
                    stableWindowMs = 1_000
                )
            }

            // file size changes every poll
            repeat(4) {
                advanceTimeBy(200)
                fakeTime += 200
                assertFalse(deferred.isCompleted)
            }

            // now it stabilizes
            repeat(5) {
                advanceTimeBy(200)
                fakeTime += 200
            }

            val result = deferred.await()
            assertEquals(40L, result?.size)
        }
}

private fun mediaStoreEntry(size: Long) =
    MediaStoreEntry(
        rowId = "1",
        absPath = "/tmp/file",
        relativePath = "Download/",
        size = size,
        isPending = false,
        isTrashed = false
    )
