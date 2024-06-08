package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.generic.milliSecondsTo
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.model.MediaStoreFileProvider
import com.w2sv.navigator.moving.MoveFile
import com.w2sv.navigator.moving.MoveMode
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
    fileTypeConfigMap: Map<FileType, FileTypeConfig>,
    contentResolver: ContentResolver,
    onNewNavigatableFileListener: (MoveFile) -> Unit,
    handler: Handler
): List<FileObserver> {
    return buildList {
        addAll(
            FileType.Media.values
                .filter { fileTypeConfigMap.getValue(it).enabled }
                .map { mediaFileType ->
                    MediaFileObserver(
                        fileType = mediaFileType,
                        enabledSourceTypeToAutoMoveConfig = fileTypeConfigMap
                            .getValue(mediaFileType)
                            .sourceTypeConfigMap
                            .filterValues { it.enabled }
                            .mapValues { it.value.autoMoveConfig },
                        contentResolver = contentResolver,
                        onNewMoveFile = onNewNavigatableFileListener,
                        handler = handler
                    )
                }
        )
        FileType.NonMedia.values
            .filter { fileTypeConfigMap.getValue(it).enabled }
            .run {
                if (isNotEmpty()) {
                    add(
                        NonMediaFileObserver(
                            enabledFileTypeToAutoMoveConfig = associateWith { fileType ->
                                fileTypeConfigMap.getValue(
                                    fileType
                                )
                                    .sourceTypeConfigMap.getValue(SourceType.Download).autoMoveConfig
                            },
                            contentResolver = contentResolver,
                            onNewMoveFile = onNewNavigatableFileListener,
                            handler = handler
                        )
                    )
                }
            }
    }
}

internal val AutoMoveConfig.moveMode: MoveMode?
    get() = enabledDestination
        ?.let { MoveMode.Auto(it) }

internal val AutoMoveConfig.enabledDestination: Uri?
    get() = if (enabled) destination else null