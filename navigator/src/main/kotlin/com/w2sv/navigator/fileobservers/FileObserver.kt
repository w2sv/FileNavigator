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
import com.w2sv.navigator.model.NavigatableFile
import slimber.log.i

internal abstract class FileObserver(
    val contentObserverUri: Uri,
    private val contentResolver: ContentResolver,
    private val onNewNavigatableFileListener: (NavigatableFile) -> Unit,
) :
    ContentObserver(Handler(Looper.getMainLooper())) {

    private val mediaStoreFileProvider: MediaStoreFile.Provider = MediaStoreFile.Provider()

    override fun deliverSelfNotifications(): Boolean = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        i { "${this::class.java.simpleName} onChange | Uri: $uri" }

        uri ?: return

        val mediaStoreFile =
            mediaStoreFileProvider.getMediaStoreFileIfNotPending(uri, contentResolver) ?: return

        when {
            !mediaStoreFile.columnData.recentlyAdded -> emitDiscardedLog("not recently added")
            cache.any { it.isIdenticalFileAs(mediaStoreFile) } -> emitDiscardedLog(
                "identical file in cache"
            )

            else -> {
                getNavigatableFileIfMatching(mediaStoreFile)
                    ?.let {
                        i { "Calling onNewNavigatableFileListener on $it" }
                        onNewNavigatableFileListener(it)
                    }

                cache.add(mediaStoreFile)
                i { "Added $mediaStoreFile to cache" }
            }
        }
    }

    private val cache =
        EvictingQueue.create<MediaStoreFile>(5)

    protected abstract fun getNavigatableFileIfMatching(
        mediaStoreFile: MediaStoreFile
    ): NavigatableFile?
}

fun emitDiscardedLog(reason: String) {
    i { "DISCARDED: $reason" }
}

internal fun getFileObservers(
    statusMap: Map<DataStoreEntry.EnumValued<FileType.Status>, FileType.Status>,
    mediaFileSourceEnabled: Map<DataStoreEntry.UniType<Boolean>, Boolean>,
    contentResolver: ContentResolver,
    onNewNavigatableFileListener: (NavigatableFile) -> Unit
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