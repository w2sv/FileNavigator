package com.w2sv.navigator.model

import android.content.ContentResolver
import android.net.Uri
import android.os.Parcelable
import com.w2sv.navigator.fileobservers.emitDiscardedLog
import kotlinx.parcelize.Parcelize
import slimber.log.i
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.security.MessageDigest

@Parcelize
data class MediaStoreFile(
    val uri: Uri,
    val columnData: MediaStoreColumnData,
    val sha256: String
) : Parcelable {

    /**
     * @return true if this & other have the same URI (=> identical path) OR
     * if they have the identical content and file name (=> same file that may have been moved to a different location).
     */
    fun isIdenticalFileAs(other: MediaStoreFile): Boolean =
        uri == other.uri || (sha256 == other.sha256 && columnData.name == other.columnData.name)

    class Provider {

        fun getMediaStoreFileIfNotPending(
            mediaUri: Uri,
            contentResolver: ContentResolver
        ): MediaStoreFile? {
            val columnData =
                MediaStoreColumnData.fetch(mediaUri, contentResolver) ?: return null

            if (columnData.isPending) {
                emitDiscardedLog("pending")
                return null
            }

            val sha256 = try {
                columnData.getFile().getContentHash(sha256MessageDigest)
                    .also { i { "SHA256 ($mediaUri) = $it" } }
            } catch (e: FileNotFoundException) {
                emitDiscardedLog(e.toString())
                return null
            }

            return MediaStoreFile(
                mediaUri,
                columnData,
                sha256
            )
        }

        // Reuse MessageDigest instance, as recommended in https://stackoverflow.com/a/13802730/12083276
        private val sha256MessageDigest = MessageDigest.getInstance("SHA-256")
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun File.getContentHash(messageDigest: MessageDigest): String {
    FileInputStream(this)
        .use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                messageDigest.update(buffer, 0, bytesRead)
            }
        }

    return messageDigest.digest().toHexString()
}