package com.w2sv.navigator.model

import android.content.ContentResolver
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import com.w2sv.common.utils.parseBoolean
import com.w2sv.common.utils.queryNonNullMediaStoreData
import com.w2sv.data.model.FileType
import com.w2sv.kotlinutils.dateFromUnixTimestamp
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import slimber.log.i
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.security.MessageDigest
import java.util.Date

/**
 * @param volumeRelativeDirPath Relative dir path from the storage volume, e.g. "Documents/", "DCIM/Camera/".
 */
@Parcelize
data class MediaStoreData(
    val rowId: String,
    val absPath: String,
    val volumeRelativeDirPath: String,
    val name: String,
    val dateAdded: Date,
    val size: Long,
    val isPending: Boolean,
    val sha256: String
) : Parcelable {

    @IgnoredOnParcel
    val recentlyAdded = !addedBeforeForMoreThan(5_000)

    fun addedBeforeForMoreThan(ms: Long): Boolean =
        (System.currentTimeMillis() - dateAdded.time) > ms

    fun pointsToSameContentAs(other: MediaStoreData): Boolean =
        rowId == other.rowId || sha256 == other.sha256

    @IgnoredOnParcel
    val fileExtension: String by lazy {
        name.substringAfterLast(".")
    }

    @IgnoredOnParcel
    val nonIncrementedNameWOExtension: String by lazy {
        name
            .substringBeforeLast(".")  // remove file extension
            .replace(  // remove trailing file incrementation parentheses
                Regex("\\(\\d+\\)$"),
                ""
            )
    }

    @IgnoredOnParcel
    val dirName: String by lazy {
        volumeRelativeDirPath
            .removeSuffix(File.separator)
            .substringAfterLast(File.separator)
    }

    fun getSourceKind(): FileType.Source.Kind =
        when {
            volumeRelativeDirPath.contains(Environment.DIRECTORY_DOWNLOADS) -> FileType.Source.Kind.Download
            // NOTE: Don't change the order of the Screenshot and Camera branches, as Environment.DIRECTORY_SCREENSHOTS
            // may be a child dir of Environment.DIRECTORY_DCIM
            volumeRelativeDirPath.contains(Environment.DIRECTORY_SCREENSHOTS) -> FileType.Source.Kind.Screenshot
            volumeRelativeDirPath.contains(Environment.DIRECTORY_DCIM) -> FileType.Source.Kind.Camera
            else -> FileType.Source.Kind.OtherApp
        }
            .also {
                i { "Determined Source.Kind: ${it.name}" }
            }

    companion object {

        fun fetch(
            uri: Uri, contentResolver: ContentResolver
        ): MediaStoreData? =
            try {
                contentResolver.queryNonNullMediaStoreData(
                    uri,
                    arrayOf(
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DATA,
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.DATE_ADDED,
                        MediaStore.MediaColumns.SIZE,
                        MediaStore.MediaColumns.IS_PENDING
                    )
                )
                    ?.run {
                        val absPath = get(1)
                        val size = get(5).toLong()
                        val sha256 = File(absPath).getSHA256()

                        MediaStoreData(
                            rowId = get(0),
                            absPath = get(1),
                            volumeRelativeDirPath = get(2),
                            name = get(3),
                            dateAdded = dateFromUnixTimestamp(get(4)),
                            size = size,
                            isPending = parseBoolean(get(6)) || size == 0L || sha256.isEmpty(),
                            sha256 = sha256
                        )
                            .also {
                                i { it.toString() }
                            }
                    }
            } catch (e: CursorIndexOutOfBoundsException) {
                i { e.toString() }
                null
            }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun File.getSHA256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return try {
        FileInputStream(this).use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        digest.digest().toHexString().also { i { "SHA256 ($name) = $it" } }
    } catch (e: FileNotFoundException) {
        i { e.toString() }
        ""
    }
}