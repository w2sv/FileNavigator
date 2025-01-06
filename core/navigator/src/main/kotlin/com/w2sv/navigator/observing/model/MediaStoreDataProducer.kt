package com.w2sv.navigator.observing.model

import android.content.ContentResolver
import com.google.common.collect.EvictingQueue
import com.w2sv.common.util.MediaUri
import com.w2sv.navigator.shared.emitDiscardedLog
import javax.inject.Inject
import javax.inject.Singleton

private const val SEEN_FILES_BUFFER_SIZE = 5

@Singleton
internal class MediaStoreDataProducer @Inject constructor() {

    sealed interface Result {
        data class Success(
            val data: MediaStoreFileData,
            val isUpdateOfAlreadySeenFile: Boolean
        ) : Result

        sealed interface Failure : Result

        data object CouldntRetrieve : Failure
        data object FileIsPending : Failure
        data object FileIsTrashed : Failure
        data object AlreadySeen : Failure

        val asSuccessOrNull: Success?
            get() = this as? Success
    }

    private data class SeenParameters(val uri: MediaUri, val fileSize: Long)

    private val seenParametersBuffer =
        EvictingQueue.create<SeenParameters>(SEEN_FILES_BUFFER_SIZE)

    operator fun invoke(mediaUri: MediaUri, contentResolver: ContentResolver): Result {
        // Fetch MediaStoreColumnData; exit if impossible
        val columnData =
            MediaStoreFileData.queryFor(mediaUri, contentResolver)
                ?: return Result.CouldntRetrieve

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

        val fileInBuffer = seenParametersBuffer.removeIf { it.uri == seenParameters.uri }
        seenParametersBuffer.add(seenParameters)
        return Result.Success(data = columnData, isUpdateOfAlreadySeenFile = fileInBuffer)
    }
}
