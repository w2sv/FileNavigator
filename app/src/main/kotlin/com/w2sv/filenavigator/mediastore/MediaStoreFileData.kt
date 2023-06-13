package com.w2sv.filenavigator.mediastore

import android.content.ContentResolver
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore.MediaColumns
import com.w2sv.kotlinutils.dateFromUnixTimestamp
import com.w2sv.kotlinutils.timeDelta
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import slimber.log.i
import java.io.File
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * @param relativePath Relative path from the storage volume, e.g. "Documents/", "DCIM/Camera/".
 */
@Parcelize
data class MediaStoreFileData(
    val id: String,
    val relativePath: String,
    val name: String,
    val dateAdded: Date,
    val size: Long,
    val isDownload: Boolean,
    val isPendingFlag: Boolean
) : Parcelable {

    val isNewlyAdded: Boolean
        get() = (timeDelta(
            dateAdded, Date(System.currentTimeMillis()), TimeUnit.SECONDS
        ) < 10).also {
            i { "isNewlyAdded: $it" }
        }

    val isPending: Boolean
        get() = (isPendingFlag || size == 0L).also {
            i { "isPending: $it" }
        }

    fun pointsToSameContentAs(other: MediaStoreFileData): Boolean =
        id == other.id || (size == other.size && nonIncrementedNameWOExtension == other.nonIncrementedNameWOExtension)    // TODO

    @IgnoredOnParcel
    val fileExtension: String by lazy {
        name.substringAfterLast(".")
    }

    @IgnoredOnParcel
    val nonIncrementedNameWOExtension: String by lazy {
        name.substringBeforeLast(".")  // remove file extension
            .replace(Regex("\\(\\d+\\)$"), "")  // remove trailing file incrementation parentheses
    }

    @IgnoredOnParcel
    val dirName: String by lazy {
        relativePath
            .removeSuffix(File.separator)
            .substringAfterLast(File.separator)
    }

    @IgnoredOnParcel
    val sourceKind: FileType.SourceKind by lazy {
        when {
            isDownload -> FileType.SourceKind.Download
            // NOTE: Don't change the order of the Screenshot and Camera branches, as the actual screenshot dir
            // may be a child dir of the camera directory
            relativePath.contains(Environment.DIRECTORY_SCREENSHOTS) -> FileType.SourceKind.Screenshot
            relativePath.contains(Environment.DIRECTORY_DCIM) -> FileType.SourceKind.Camera
            else -> FileType.SourceKind.ThirdPartyApp
        }.also {
            i {
                "relativePath: $relativePath\nDetermined OriginKind: ${it.name}"
            }
        }
    }

    companion object {

        fun fetch(
            uri: Uri, contentResolver: ContentResolver
        ): MediaStoreFileData? = try {
            contentResolver.queryNonNullMediaStoreData(
                uri, arrayOf(
                    MediaColumns._ID,
                    MediaColumns.RELATIVE_PATH,
                    MediaColumns.DISPLAY_NAME,
                    MediaColumns.DATE_ADDED,
                    MediaColumns.SIZE,
                    MediaColumns.IS_DOWNLOAD,
                    MediaColumns.IS_PENDING
                )
            )?.run {
                i { "Raw mediaStoreColumns: ${toList()}" }

                MediaStoreFileData(
                    id = get(0),
                    relativePath = get(1),
                    name = get(2),
                    dateAdded = dateFromUnixTimestamp(get(3)),
                    size = get(4).toLong(),
                    isDownload = parseBoolean(get(5)),
                    isPendingFlag = parseBoolean(get(6))
                )
            }
        } catch (e: CursorIndexOutOfBoundsException) {
            i { e.toString() }
            null
        }
    }
}

private fun parseBoolean(mediaStoreString: String): Boolean = when (mediaStoreString) {
    "0" -> false
    "1" -> true
    else -> throw IllegalStateException()
}