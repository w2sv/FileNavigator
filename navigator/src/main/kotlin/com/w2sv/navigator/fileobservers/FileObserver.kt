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
import com.w2sv.navigator.model.MediaStoreData
import com.w2sv.navigator.model.MoveFile
import slimber.log.i

internal abstract class FileObserver(
    val contentObserverUri: Uri,
    private val contentResolver: ContentResolver,
    private val onNewMoveFile: (MoveFile) -> Unit
) :
    ContentObserver(Handler(Looper.getMainLooper())) {

    override fun deliverSelfNotifications(): Boolean = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        i { "onChange | Uri: $uri" }

        uri ?: return

        MediaStoreData.fetch(uri, contentResolver)?.let { mediaStoreFileData ->
            if (mediaStoreFileData.isPending) return@let

            if (mediaStoreFileData.isNewlyAdded &&
                mediaStoreFileDataBlacklist.none {
                    it.pointsToSameContentAs(
                        mediaStoreFileData
                    )
                }
            ) {
                getMoveFileIfMatching(mediaStoreFileData, uri)?.let(onNewMoveFile)
            }

            mediaStoreFileDataBlacklist.add(mediaStoreFileData)
        }
    }

    protected abstract fun getMoveFileIfMatching(mediaStoreFileData: MediaStoreData, uri: Uri): MoveFile?

    private val mediaStoreFileDataBlacklist =
        EvictingQueue.create<MediaStoreData>(5)
}

internal fun getFileObservers(
    statusMap: Map<FileType.Status.StoreEntry, FileType.Status>,
    mediaFileSourceEnabled: Map<DataStoreEntry.UniType<Boolean>, Boolean>,
    contentResolver: ContentResolver,
    onNewMoveFile: (MoveFile) -> Unit
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