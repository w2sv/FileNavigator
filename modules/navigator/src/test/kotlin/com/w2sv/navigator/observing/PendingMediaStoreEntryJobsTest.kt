package com.w2sv.navigator.observing

import com.w2sv.common.uri.MediaId
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PendingMediaStoreEntryJobsTest {

    @Test
    fun `cancelled old job does not remove current replacement`() =
        runTest {
            val jobs = PendingMediaStoreEntryJobs()
            val mediaId = MediaId(1)
            val firstJobCancelled = CompletableDeferred<Unit>()

            assertFalse(
                jobs.replace(mediaId, this) {
                    try {
                        awaitCancellation()
                    } finally {
                        firstJobCancelled.complete(Unit)
                    }
                }
            )
            runCurrent()

            assertTrue(
                jobs.replace(mediaId, this) {
                    awaitCancellation()
                }
            )
            firstJobCancelled.await()

            assertTrue(jobs.cancelAndRemove(mediaId))
        }

    @Test
    fun `completed current job removes itself`() =
        runTest {
            val jobs = PendingMediaStoreEntryJobs()
            val mediaId = MediaId(1)

            assertFalse(jobs.replace(mediaId, this) {})
            runCurrent()

            assertFalse(jobs.cancelAndRemove(mediaId))
        }
}
