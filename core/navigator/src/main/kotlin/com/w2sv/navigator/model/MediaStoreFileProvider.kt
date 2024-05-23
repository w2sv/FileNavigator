package com.w2sv.navigator.model

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.google.common.collect.EvictingQueue
import com.w2sv.navigator.fileobservers.emitDiscardedLog
import slimber.log.i
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.security.MessageDigest

internal class MediaStoreFileProvider {

    sealed interface Result {
        data class Success(val mediaStoreFile: MediaStoreFile) : Result
        data object CouldntFetchMediaStoreColumnData : Result
        data object FileIsPending : Result
        data object FileNotFoundException : Result
        data object AlreadySeen : Result
    }

    private data class SeenParameters(val uri: Uri, val fileSize: Long)

    private val seenParametersCache = EvictingQueue.create<SeenParameters>(5)

    fun getMediaStoreFileIfNotPendingAndNotAlreadySeen(
        mediaUri: Uri,
        contentResolver: ContentResolver
    ): Result {
        // Fetch MediaStoreColumnData; exit if impossible
        val columnData =
            MediaStoreColumnData.fetch(mediaUri, contentResolver)
                ?: return Result.CouldntFetchMediaStoreColumnData

        // Exit if file is pending
        if (columnData.isPending) {
            emitDiscardedLog { "pending" }
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
                uri = mediaUri,
                columnData = columnData,
                sha256 = sha256
            )
        )
    }

    // Reuse MessageDigest instance, as recommended in https://stackoverflow.com/a/13802730/12083276
    private val sha256MessageDigest = MessageDigest.getInstance("SHA-256")
}

@OptIn(ExperimentalStdlibApi::class)
@VisibleForTesting
internal fun File.contentHash(
    messageDigest: MessageDigest,
    bufferSize: Int = 8192,
): String {
    FileInputStream(this)
        .use { inputStream ->
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                messageDigest.update(buffer, 0, bytesRead)
            }
        }

    return messageDigest.digest().toHexString()
}