package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.generic.milliSecondsTo
import com.w2sv.domain.model.FileType
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.model.MediaStoreFileProvider
import com.w2sv.navigator.model.MoveFile
import slimber.log.i
import java.time.LocalDateTime

internal abstract class FileObserver(
    val contentObserverUri: Uri,
    private val contentResolver: ContentResolver,
    private val onNewMoveFileListener: (MoveFile) -> Unit,
) :
    ContentObserver(Handler(Looper.getMainLooper())) {

    private val mediaStoreFileProvider: MediaStoreFileProvider = MediaStoreFileProvider()

    protected abstract fun getLogIdentifier(): String

    override fun deliverSelfNotifications(): Boolean = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        i { "${getLogIdentifier()} onChange | Uri: $uri" }

        uri ?: return

        val changeObservationDateTime = LocalDateTime.now()

        when (val result =
            mediaStoreFileProvider.getMediaStoreFileIfNotPending(uri, contentResolver)) {
            is MediaStoreFileProvider.Result.Success -> {
                when {
                    latestCutCandidate?.matches(
                        CutPasteCandidate(uri, changeObservationDateTime),
                        500
                    ) == true -> {
                        cache.add(result.mediaStoreFile)
                        emitDiscardedLog { "Move" }
                    }

                    !result.mediaStoreFile.columnData.recentlyAdded -> emitDiscardedLog { "not recently added" }

                    cache.any { it.isIdenticalFileAs(result.mediaStoreFile) } -> emitDiscardedLog { "identical file in cache" }

                    else -> {
                        getMoveFileIfMatching(result.mediaStoreFile)
                            ?.let {
                                i { "Calling onNewNavigatableFileListener on $it" }
                                onNewMoveFileListener(it)
                            }

                        cache.add(result.mediaStoreFile)
                        i { "Added ${result.mediaStoreFile} to cache" }
                    }
                }
            }

            else -> {
                if (result is MediaStoreFileProvider.Result.CouldntFetchMediaStoreColumnData) {
                    latestCutCandidate = CutPasteCandidate(uri, changeObservationDateTime)
                }
            }
        }
    }

    private val cache =
        EvictingQueue.create<MediaStoreFile>(5)

    private var latestCutCandidate: CutPasteCandidate? = null

    protected abstract fun getMoveFileIfMatching(
        mediaStoreFile: MediaStoreFile
    ): MoveFile?
}

private data class CutPasteCandidate(val uri: Uri, val changeObservationDateTime: LocalDateTime) {

    fun matches(other: CutPasteCandidate, timeThreshold: Int): Boolean =
        uri != other.uri && changeObservationDateTime.milliSecondsTo(other.changeObservationDateTime) < timeThreshold
}

internal fun emitDiscardedLog(reason: () -> String) {
    i { "DISCARDED: ${reason()}" }
}

internal fun getFileObservers(
    fileTypeEnablementMap: Map<FileType, Boolean>,
    mediaFileSourceEnablementMap: Map<FileType.Source, Boolean>,
    contentResolver: ContentResolver,
    onNewNavigatableFileListener: (MoveFile) -> Unit
): List<FileObserver> =
    buildList {
        addAll(
            FileType.Media.getValues()
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
                        onNewMoveFile = onNewNavigatableFileListener
                    )
                }
        )
        FileType.NonMedia.getValues()
            .filter { fileTypeEnablementMap.getValue(it) }
            .run {
                if (isNotEmpty()) {
                    add(
                        NonMediaFileObserver(
                            fileTypes = this,
                            contentResolver = contentResolver,
                            onNewMoveFile = onNewNavigatableFileListener
                        )
                    )
                }
            }
    }