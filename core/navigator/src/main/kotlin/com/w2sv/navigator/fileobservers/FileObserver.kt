package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.generic.milliSecondsTo
import com.w2sv.domain.model.FileTypeKind
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.model.MediaStoreFileProvider
import com.w2sv.navigator.moving.MoveFile
import slimber.log.i
import java.time.LocalDateTime

internal abstract class FileObserver(
    val contentObserverUri: Uri,
    private val contentResolver: ContentResolver,
    private val onNewMoveFileListener: (MoveFile) -> Unit,
    handler: Handler
) :
    ContentObserver(handler) {

    private val mediaStoreFileProvider: MediaStoreFileProvider = MediaStoreFileProvider()

    protected abstract fun getLogIdentifier(): String

    override fun deliverSelfNotifications(): Boolean = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        i { "${getLogIdentifier()} onChange | Uri: $uri" }

        uri ?: return

        val changeObservationDateTime = LocalDateTime.now()
        val manualMoveCandidate by lazy {
            ManualMoveCandidate(
                uri = uri,
                changeObservationDateTime = changeObservationDateTime
            )
        }
        val mediaStoreFileProvisionResult =
            mediaStoreFileProvider.getMediaStoreFileIfNotPendingAndNotAlreadySeen(
                mediaUri = uri,
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

private data class ManualMoveCandidate(val uri: Uri, val changeObservationDateTime: LocalDateTime) {

    fun matches(other: ManualMoveCandidate, milliSecondsThreshold: Int): Boolean =
        uri != other.uri && changeObservationDateTime.milliSecondsTo(other.changeObservationDateTime) < milliSecondsThreshold
}

internal fun emitDiscardedLog(reason: () -> String) {
    i { "DISCARDED: ${reason()}" }
}

internal fun getFileObservers(
    fileTypeEnablementMap: Map<FileTypeKind, Boolean>,
    mediaFileSourceEnablementMap: Map<FileTypeKind.Source, Boolean>,
    contentResolver: ContentResolver,
    onNewNavigatableFileListener: (MoveFile) -> Unit,
    handler: Handler
): List<FileObserver> {
    return buildList {
        addAll(
            FileTypeKind.Media.values
                .filter { fileTypeEnablementMap.getValue(it) }
                .map { mediaType ->
                    MediaFileObserver(
                        fileType = mediaType,
                        sourceKinds = mediaType
                            .sources
                            .filter { source -> mediaFileSourceEnablementMap.getValue(source) }
                            .map { source -> source.kind }
                            .toSet(),
                        contentResolver = contentResolver,
                        onNewMoveFile = onNewNavigatableFileListener,
                        handler = handler
                    )
                }
        )
        FileTypeKind.NonMedia.values
            .filter { fileTypeEnablementMap.getValue(it) }
            .run {
                if (isNotEmpty()) {
                    add(
                        NonMediaFileObserver(
                            fileTypes = this,
                            contentResolver = contentResolver,
                            onNewMoveFile = onNewNavigatableFileListener,
                            handler = handler
                        )
                    )
                }
            }
    }
}