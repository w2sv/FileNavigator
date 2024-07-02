package com.w2sv.navigator.observing.model

import android.content.ContentResolver
import com.google.common.collect.EvictingQueue
import com.w2sv.common.utils.MediaUri
import com.w2sv.navigator.shared.emitDiscardedLog
import javax.inject.Inject
import javax.inject.Singleton

private const val SEEN_FILES_BUFFER_SIZE = 5

@Singleton
internal class MediaStoreDataProducer @Inject constructor() {

    sealed interface Result {
        data class Success(val data: MediaStoreData) : Result
        data object CouldntRetrieveMediaStoreData : Result
        data object FileIsPending : Result
        data object FileIsTrashed : Result
        data object AlreadySeen : Result
    }

    private data class SeenParameters(val uri: MediaUri, val fileSize: Long)

    private val seenParametersBuffer =
        EvictingQueue.create<SeenParameters>(SEEN_FILES_BUFFER_SIZE)

    fun mediaStoreDataOrNull(
        mediaUri: MediaUri,
        contentResolver: ContentResolver
    ): MediaStoreData? =
        (invoke(mediaUri, contentResolver) as? Result.Success)?.data

    operator fun invoke(
        mediaUri: MediaUri,
        contentResolver: ContentResolver
    ): Result {
        // Fetch MediaStoreColumnData; exit if impossible
        val columnData =
            MediaStoreData.queryFor(mediaUri, contentResolver)
                ?: return Result.CouldntRetrieveMediaStoreData

        // Exit if file is pending or trashed
        if (columnData.isPending) {
            emitDiscardedLog { "pending" }
            return Result.FileIsPending
        }
        if (columnData.isTrashed) {
            emitDiscardedLog { "trashed" }
            return Result.FileIsTrashed
        }

        val seenParameters = SeenParameters(uri = mediaUri, fileSize = columnData.size)
        if (seenParametersBuffer.contains(seenParameters)) {
            emitDiscardedLog { "already seen" }
            return Result.AlreadySeen
        }

        seenParametersBuffer.add(seenParameters)
        return Result.Success(columnData)
    }
}