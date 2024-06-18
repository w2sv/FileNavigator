package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.generic.milliSecondsTo
import com.w2sv.common.utils.MediaUri
import com.w2sv.navigator.mediastore.MediaStoreFile
import com.w2sv.navigator.mediastore.MediaStoreFileProvider
import com.w2sv.navigator.moving.MoveFile
import com.w2sv.navigator.shared.emitDiscardedLog
import slimber.log.i
import java.time.LocalDateTime

internal abstract class FileObserver(
    private val contentResolver: ContentResolver,
    private val onNewMoveFileListener: (MoveFile) -> Unit,
    private val mediaStoreFileProvider: MediaStoreFileProvider,
    handler: Handler
) :
    ContentObserver(handler) {

    protected abstract val logIdentifier: String

    override fun deliverSelfNotifications(): Boolean = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        i { "$logIdentifier onChange | Uri: $uri" }

        val mediaUri = uri?.let { MediaUri(it) } ?: return

        val changeObservationDateTime = LocalDateTime.now()
        val manualMoveCandidate by lazy {
            ManualMoveCandidate(
                mediaUri = mediaUri,
                changeObservationDateTime = changeObservationDateTime
            )
        }
        val mediaStoreFileProvisionResult =
            mediaStoreFileProvider.getMediaStoreFileIfNotPendingAndNotAlreadySeen(
                mediaUri = mediaUri,
                contentResolver = contentResolver
            )

        when (mediaStoreFileProvisionResult) {
            is MediaStoreFileProvider.Result.Success -> {
                when {
                    matchesLatestManualMoveCandidate(manualMoveCandidate) -> {
                        seenMediaStoreFiles.add(mediaStoreFileProvisionResult.mediaStoreFile)
                        emitDiscardedLog { "Manually moved file" }
                    }

                    seenMediaStoreFiles.any { it.isIdenticalFileAs(mediaStoreFileProvisionResult.mediaStoreFile) } -> emitDiscardedLog { "Identical file in cache" }

                    else -> {
                        getMoveFileIfMatchingConstraints(mediaStoreFileProvisionResult.mediaStoreFile)
                            ?.let {
                                i { "Calling onNewNavigatableFileListener on $it" }
                                onNewMoveFileListener(it)
                            }

                        seenMediaStoreFiles.add(mediaStoreFileProvisionResult.mediaStoreFile)
                        i { "Added ${mediaStoreFileProvisionResult.mediaStoreFile} to cache" }
                    }
                }
            }

            is MediaStoreFileProvider.Result.CouldntFetchMediaStoreColumnData -> {
                latestManualMoveCandidate = manualMoveCandidate
            }

            else -> Unit
        }
    }

    private val seenMediaStoreFiles =
        EvictingQueue.create<MediaStoreFile>(5)

    private var latestManualMoveCandidate: ManualMoveCandidate? = null

    private fun matchesLatestManualMoveCandidate(candidate: ManualMoveCandidate): Boolean =
        latestManualMoveCandidate?.matches(
            other = candidate,
            milliSecondsThreshold = 500
        ) == true

    protected abstract fun getMoveFileIfMatchingConstraints(
        mediaStoreFile: MediaStoreFile
    ): MoveFile?
}

private data class ManualMoveCandidate(
    val mediaUri: MediaUri,
    val changeObservationDateTime: LocalDateTime
) {
    fun matches(other: ManualMoveCandidate, milliSecondsThreshold: Int): Boolean =
        mediaUri != other.mediaUri && changeObservationDateTime.milliSecondsTo(other.changeObservationDateTime) < milliSecondsThreshold
}