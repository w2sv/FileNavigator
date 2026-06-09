package com.w2sv.navigator.observing

import com.w2sv.storage.uri.MediaId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Tracks the single active media-store stability job per [MediaId].
 *
 * A media row can emit several update events while the file is settling. Replacing
 * the active job must cancel the previous one without letting that canceled job's
 * `finally` block remove the newer replacement from the map.
 */
internal class PendingMediaStoreEntryJobs {
    /**
     * Guards [jobs] because replacement, explicit cancellation, and coroutine
     * completion can run from different observer/coroutine contexts. The lock keeps
     * "cancel old, install new" and "remove only if current" atomic relative to
     * each other.
     */
    private val lock = Any()
    private val jobs = mutableMapOf<MediaId, Job>()

    /**
     * Cancels any currently tracked job for [mediaId], installs a new job running
     * [block], and starts it after the map has been updated.
     * @return whether a previous job existed and was canceled.
     */
    fun replace(mediaId: MediaId, scope: CoroutineScope, block: suspend () -> Unit): Boolean {
        val newJob = scope.launch(start = CoroutineStart.LAZY) {
            try {
                block()
            } finally {
                removeIfCurrent(mediaId, coroutineContext[Job])
            }
        }

        val cancelledPrevious = synchronized(lock) {
            val previousJob = jobs.remove(mediaId)?.also(Job::cancel)
            jobs[mediaId] = newJob
            previousJob != null
        }

        newJob.start()
        return cancelledPrevious
    }

    /**
     * Cancels and removes the currently tracked job for [mediaId].
     * @return whether a job existed and was canceled.
     */
    fun cancelAndRemove(mediaId: MediaId): Boolean =
        synchronized(lock) {
            jobs.remove(mediaId)?.also(Job::cancel) != null
        }

    /**
     * Removes [job] only if it is still the current job for [mediaId].
     */
    private fun removeIfCurrent(mediaId: MediaId, job: Job?) {
        synchronized(lock) {
            if (jobs[mediaId] === job) {
                jobs.remove(mediaId)
            }
        }
    }
}
