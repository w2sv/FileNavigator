package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.data.model.FileType
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.model.MoveFile
import slimber.log.i
import java.util.Date

internal abstract class FileObserver(
    val contentObserverUri: Uri,
    private val contentResolver: ContentResolver,
    private val onNewMoveFileListener: (MoveFile) -> Unit,
) :
    ContentObserver(Handler(Looper.getMainLooper())) {

    private val mediaStoreFileProvider: MediaStoreFile.Provider = MediaStoreFile.Provider()

    protected abstract val logIdentifier: String

    override fun deliverSelfNotifications(): Boolean = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        i { "$logIdentifier onChange | Uri: $uri" }

        uri ?: return

        val changeObservationTime = Date()

        when (val result =
            mediaStoreFileProvider.getMediaStoreFileIfNotPending(uri, contentResolver)) {
            is MediaStoreFile.Provider.Result.Success -> {
                when {
                    latestCutCandidate?.matches(
                        PasteCandidate(uri, changeObservationTime),
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
                if (result is MediaStoreFile.Provider.Result.CouldntFetchMediaStoreColumnData) {
                    latestCutCandidate = CutCandidate(uri, changeObservationTime)
                }
            }
        }
    }

    private val cache =
        EvictingQueue.create<MediaStoreFile>(5)

    private var latestCutCandidate: CutCandidate? = null

    protected abstract fun getMoveFileIfMatching(
        mediaStoreFile: MediaStoreFile
    ): MoveFile?
}

fun emitDiscardedLog(reason: () -> String) {
    i { "DISCARDED: ${reason()}" }
}

internal fun getFileObservers(
    statusMap: Map<DataStoreEntry.EnumValued<FileType.Status>, FileType.Status>,
    mediaFileSourceEnabled: Map<DataStoreEntry.UniType<Boolean>, Boolean>,
    contentResolver: ContentResolver,
    onNewNavigatableFileListener: (MoveFile) -> Unit
): List<FileObserver> {
    val mediaFileObservers = FileType.Media.getValues()
        .filterEnabled(statusMap)
        .map { mediaType ->
            MediaFileObserver(
                fileType = mediaType,
                sourceKinds = mediaType
                    .sources
                    .filter { source -> mediaFileSourceEnabled.getValue(source.isEnabledDSE) }
                    .map { source -> source.kind }
                    .toSet(),
                contentResolver = contentResolver,
                onNewMoveFile = onNewNavigatableFileListener
            )
        }

    val nonMediaFileObserver =
        FileType.NonMedia.getValues()
            .filterEnabled(statusMap)
            .run {
                if (isNotEmpty()) {
                    NonMediaFileObserver(
                        fileTypes = this,
                        contentResolver = contentResolver,
                        onNewMoveFile = onNewNavigatableFileListener
                    )
                } else {
                    null
                }
            }

    return buildList {
        addAll(mediaFileObservers)
        nonMediaFileObserver?.let(::add)
    }
}

fun <FT : FileType> Iterable<FT>.filterEnabled(statusMap: Map<DataStoreEntry.EnumValued<FileType.Status>, FileType.Status>): List<FT> =
    filter { statusMap.getValue(it.statusDSE).isEnabled }