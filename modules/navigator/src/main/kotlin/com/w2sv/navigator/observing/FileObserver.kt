package com.w2sv.navigator.observing

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.moving.MediaStoreEntry
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.NavigatableFile
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.storage.uri.MediaId
import com.w2sv.storage.uri.MediaUri
import com.w2sv.storage.uri.mediaUri
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import slimber.log.i

internal abstract class FileObserver(val mediaType: MediaType, blacklistSize: Int, handler: Handler, environment: FileObserverEnvironment) :
    ContentObserver(handler),
    FileObserverEnvironment by environment {

    /**
     * Contains IDs of files that either
     * - have been created by the app during the move operation
     * - don't match the file and source type settings the observer has been registered for
     * - a procedure job has already been triggered for
     */
    private val blacklist = RecentSet<MediaId>(blacklistSize)

    private val pendingJobs = PendingMediaStoreEntryJobs()

    open val logIdentifier: String
        get() = this::class.java.simpleName

    init {
        blacklistFilteredSelfCreatedFiles()
    }

    private fun blacklistFilteredSelfCreatedFiles() {
        selfCreatedFilesFlow
            .filter { it.mediaType == mediaType }
            .map { it.mediaId }
            .collectOn(scope) { mediaId ->
                log { "Collected $mediaId" }
                blacklist.add(mediaId)
                // If media id of a self created file corresponds to a pending job, cancel & remove it
                cancelAndRemovePendingJob(mediaId)
            }
    }

    final override fun deliverSelfNotifications(): Boolean =
        false

    // ===================
    //  onChange
    // ===================

    final override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
        onChangeLog(uris = uri.toString(), flags = flags)

        withMediaUriAndId(uri) { mediaUri, mediaId ->
            when (FileChangeEvent.parseFrom(flags)) {
                FileChangeEvent.Update -> onChangeCore(mediaUri, mediaId)
                FileChangeEvent.Delete -> cancelAndRemovePendingJob(mediaId)
                else -> Unit
            }
        }
    }

    /**
     * Called on android versions that don't provide notify flags.
     */
    final override fun onChange(selfChange: Boolean, uri: Uri?) {
        onChangeLog(uri.toString(), null)
        withMediaUriAndId(uri, ::onChangeCore)
    }

    private fun onChangeCore(mediaUri: MediaUri, mediaId: MediaId) {
        if (blacklist.contains(mediaId)) return discardedLog { "mediaId blacklisted" }

        val mediaStoreEntry = validMediaStoreEntryOrNull(mediaUri)?.also { log { "$it" } } ?: return
        val fileAndSourceType = enabledFileAndSourceTypeOrNull(mediaStoreEntry) ?: return run {
            discardedLog { "Couldn't find matching fileAndSourceType - Adding $mediaId to blacklist" }
            blacklist.add(mediaId)
        }

        log { "Found matching fileAndSourceType $fileAndSourceType" }
        promptNavigationWhenMediaStoreEntryStable(mediaId, mediaUri, fileAndSourceType)
    }

    /**
     * Returns the [FileAndSourceType] corresponding to the [mediaStoreEntry] IF it matches the user's settings.
     * This is what decides if a procedure is triggered for a file or not.
     */
    protected abstract fun enabledFileAndSourceTypeOrNull(mediaStoreEntry: MediaStoreEntry): FileAndSourceType?

    private fun promptNavigationWhenMediaStoreEntryStable(mediaId: MediaId, mediaUri: MediaUri, fileAndSourceType: FileAndSourceType) {
        val cancelledJob = pendingJobs.replace(mediaId, scope) {
            awaitStableMediaStoreEntry(
                provideEntry = { validMediaStoreEntryOrNull(mediaUri) },
                log = ::log
            )?.let { mediaStoreEntry ->
                val navigatableFile = NavigatableFile(
                    mediaUri = mediaUri,
                    mediaStoreEntry = mediaStoreEntry,
                    fileAndSourceType = fileAndSourceType
                )
                log { "Calling promptFileNavigation for $navigatableFile" }
                promptNavigation(navigatableFile)

                log { "Completed job for $mediaId - Adding to blacklist" }
                blacklist.add(mediaId)
            }
        }
        log {
            if (cancelledJob) {
                "Relaunching awaitStableMediaStoreData for $mediaId"
            } else {
                "Launching awaitStableMediaStoreData for $mediaId"
            }
        }
    }

    private suspend fun promptNavigation(navigatableFile: NavigatableFile) {
        // TODO maybe cache via StateFlows
        val autoMoveDestination = navigatorConfigFlow
            .first()
            .autoMoveConfig(navigatableFile.fileType, navigatableFile.sourceType)
            .enabledDestinationOrNull

        // Perform auto move or post move file notification
        if (autoMoveDestination == null) {
            log { "Posting move file notification for $navigatableFile" }
            notificationEventHandler(NotificationEvent.PostNavigateFile(navigatableFile))
        } else {
            log { "Performing auto move for $navigatableFile" }
            fileMover(
                operation = MoveOperation.AutoMove(
                    file = navigatableFile,
                    destination = autoMoveDestination,
                    destinationSelectionManner = DestinationSelectionManner.Auto
                ),
                context = context
            )
        }
    }

    // ===================
    //  Helpers
    // ===================

    /**
     * Invokes [block] with converted mediaUri & mediaId or logs and returns if either the passed [uri]
     * or the parsed mediaId is null.
     */
    private fun withMediaUriAndId(uri: Uri?, block: (MediaUri, MediaId) -> Unit) {
        val mediaUri = uri?.mediaUri ?: return discardedLog { "uri null" }
        val mediaId = mediaUri.id() ?: return discardedLog { "mediaId null" }
        block(mediaUri, mediaId)
    }

    private fun cancelAndRemovePendingJob(mediaId: MediaId): Boolean {
        val cancelledJob = pendingJobs.cancelAndRemove(mediaId).also { cancelled ->
            if (cancelled) {
                log { "Cancelling pending job for $mediaId" }
            }
        }
        return cancelledJob
    }

    private fun validMediaStoreEntryOrNull(mediaUri: MediaUri): MediaStoreEntry? {
        val data = MediaStoreEntry.queryFor(mediaUri, context.contentResolver)

        return when {
            data == null -> null.also { discardedLog { "couldn't query MediaStoreFileData" } }
            data.isPending -> null.also { discardedLog { "file pending" } }
            data.isTrashed -> null.also { discardedLog { "file trashed" } }
            else -> data
        }
    }

    // ===================
    //  Logging
    // ===================

    protected fun log(message: () -> String) {
        i { "$logIdentifier - ${message()}" }
    }

    protected fun discardedLog(reason: () -> String) {
        log { "Discarded file: ${reason()}" }
    }

    private fun onChangeLog(uris: String, flags: Int?) {
        log { "onChange flags=${flags?.let { FileChangeEvent.describeNotifyFlags(it) }} | $uris | blacklist=$blacklist" }
    }
}

private val AutoMoveConfig.enabledDestinationOrNull: MoveDestination.Directory?
    get() = destination?.takeIf { enabled }?.run(MoveDestination::Directory)
