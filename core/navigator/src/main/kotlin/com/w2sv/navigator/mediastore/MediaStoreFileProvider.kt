package com.w2sv.navigator.mediastore

import android.content.ContentResolver
import com.google.common.collect.EvictingQueue
import com.w2sv.common.utils.MediaUri
import com.w2sv.navigator.shared.NavigatorConstant
import com.w2sv.navigator.shared.emitDiscardedLog
import slimber.log.i
import java.io.FileNotFoundException
import java.security.MessageDigest
import javax.inject.Inject

internal class MediaStoreFileProvider @Inject constructor() {

    sealed interface Result {
        data class Success(val mediaStoreFile: MediaStoreFile) : Result
        data object CouldntFetchMediaStoreColumnData : Result
        data object FileIsPending : Result
        data object FileNotFoundException : Result
        data object AlreadySeen : Result
    }

    private data class SeenParameters(val uri: MediaUri, val fileSize: Long)

    private val seenParametersCache =
        EvictingQueue.create<SeenParameters>(NavigatorConstant.SEEN_FILE_BUFFER_SIZE)

    fun getMediaStoreFileIfNotPendingAndNotAlreadySeen(
        mediaUri: MediaUri,
        contentResolver: ContentResolver
    ): Result {
        // Fetch MediaStoreColumnData; exit if impossible
        val columnData =
            MediaStoreColumnData.fetch(mediaUri, contentResolver)
                ?: return Result.CouldntFetchMediaStoreColumnData

        // Exit if file is pending or trashed
        if (columnData.isPending) {
            emitDiscardedLog { "pending" }
            return Result.FileIsPending
        }
        if (columnData.isTrashed) {
            emitDiscardedLog { "trashed" }
            return Result.FileIsPending
        }

        // Create SeenParameters & exit if in seenParametersCache to avoid expensive recomputation of content hash
        val seenParameters = SeenParameters(uri = mediaUri, fileSize = columnData.size)
        if (seenParametersCache.contains(seenParameters)) {
            emitDiscardedLog { "already seen" }
            return Result.AlreadySeen
        }

        val sha256 = try {
            columnData.getFile().contentHash(sha256MessageDigest)
                .also { i { "SHA256 ($mediaUri) = $it" } }
//            measureTimedValue {
//                columnData.getFile().contentHash(sha256MessageDigest)
//            }
//                .also { i { "SHA256 ($mediaUri) = ${it.value}/nComputation took ${it.duration}" } }
//                .value
        } catch (e: FileNotFoundException) {
            emitDiscardedLog(e::toString)
            return Result.FileNotFoundException
        }

        seenParametersCache.add(seenParameters)
        return Result.Success(
            MediaStoreFile(
                mediaUri = mediaUri,
                columnData = columnData,
                sha256 = sha256
            )
        )
    }

    // Reuse MessageDigest instance, as recommended in https://stackoverflow.com/a/13802730/12083276
    private val sha256MessageDigest = MessageDigest.getInstance("SHA-256")
}