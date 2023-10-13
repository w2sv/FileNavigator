package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.data.model.FileType
import com.w2sv.data.model.filterEnabled
import com.w2sv.navigator.model.MediaStoreFile
import com.w2sv.navigator.model.NavigatableFile
import slimber.log.i

internal abstract class FileObserver(
    val contentObserverUri: Uri,
    private val contentResolver: ContentResolver,
    private val onNewMoveFileListener: (NavigatableFile) -> Unit
) :
    ContentObserver(Handler(Looper.getMainLooper())) {

    override fun deliverSelfNotifications(): Boolean = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        i { "${this::class.java.simpleName} onChange | Uri: $uri" }

        uri ?: return

        val mediaStoreFile = MediaStoreFile.getIfNotPending(uri, contentResolver) ?: return

        when {
            !mediaStoreFile.columnData.recentlyAdded -> emitDiscardedLog("not recently added")
            cache.any { it.isIdenticalFileAs(mediaStoreFile) } -> emitDiscardedLog(
                "identical file in cache"
            )

            else -> {
                getMoveFileIfMatching(mediaStoreFile)
                    ?.let(onNewMoveFileListener)

                cache.add(mediaStoreFile)
                i { "Added ${mediaStoreFile.sha256} to cache" }
            }
        }
    }

    private val cache =
        EvictingQueue.create<MediaStoreFile>(5)

    protected abstract fun getMoveFileIfMatching(
        mediaStoreFile: MediaStoreFile
    ): NavigatableFile?
}

fun emitDiscardedLog(reason: String) {
    i { "DISCARDED: $reason" }
}

internal fun getFileObservers(
    statusMap: Map<FileType.Status.StoreEntry, FileType.Status>,
    mediaFileSourceEnabled: Map<DataStoreEntry.UniType<Boolean>, Boolean>,
    contentResolver: ContentResolver,
    onNewMoveFile: (NavigatableFile) -> Unit
): List<FileObserver> {
    val mediaFileObservers = FileType.Media.values
        .filterEnabled(statusMap)
        .map { mediaType ->
            MediaFileObserver(
                fileType = mediaType,
                sourceKinds = mediaType
                    .sources
                    .filter { source -> mediaFileSourceEnabled.getValue(source.isEnabled) }
                    .map { source -> source.kind }
                    .toSet(),
                contentResolver = contentResolver,
                onNewMoveFile = onNewMoveFile
            )
        }

    val nonMediaFileObserver =
        FileType.NonMedia.values
            .filterEnabled(statusMap)
            .run {
                if (isNotEmpty()) {
                    NonMediaFileObserver(
                        fileTypes = this,
                        contentResolver = contentResolver,
                        onNewMoveFile = onNewMoveFile
                    )
                } else {
                    null
                }
            }

    return buildList {
        addAll(mediaFileObservers)
        nonMediaFileObserver?.let {
            add(it)
        }
    }
}