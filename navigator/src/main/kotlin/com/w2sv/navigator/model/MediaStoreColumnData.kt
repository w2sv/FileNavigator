package com.w2sv.navigator.model

import android.content.ContentResolver
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import com.w2sv.androidutils.generic.localDateTimeFromUnixTimeStamp
import com.w2sv.androidutils.generic.milliSecondsToNow
import com.w2sv.common.utils.parseBoolean
import com.w2sv.common.utils.queryNonNullMediaStoreData
import com.w2sv.data.model.FileType
import com.w2sv.navigator.fileobservers.emitDiscardedLog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import slimber.log.i
import java.io.File
import java.time.LocalDateTime

/**
 * @param volumeRelativeDirPath Relative dir path from the storage volume, e.g. "Documents/", "DCIM/Camera/".
 */
@Parcelize
data class MediaStoreColumnData(
    val rowId: String,
    val absPath: String,
    val volumeRelativeDirPath: String,
    val name: String,
    val dateTimeAdded: LocalDateTime,
    val size: Long,
    val isPending: Boolean
) : Parcelable {

    @IgnoredOnParcel
    val recentlyAdded = !addedBeforeForMoreThan(5_000)

    fun addedBeforeForMoreThan(ms: Long): Boolean =
        dateTimeAdded.milliSecondsToNow() > ms

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

    fun getFile(): File =
        File(absPath)

    val fileExists: Boolean
        get() = getFile().exists()

    fun getSourceKind(): FileType.Source.Kind =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && volumeRelativeDirPath.contains(
                Environment.DIRECTORY_RECORDINGS
            ) || volumeRelativeDirPath.contains("Recordings") -> FileType.Source.Kind.Recording
            // NOTE: Don't change the order of the Screenshot and Camera branches, as Environment.DIRECTORY_SCREENSHOTS
            // may be a child dir of Environment.DIRECTORY_DCIM
            volumeRelativeDirPath.contains(Environment.DIRECTORY_SCREENSHOTS) -> FileType.Source.Kind.Screenshot
            volumeRelativeDirPath.contains(Environment.DIRECTORY_DCIM) -> FileType.Source.Kind.Camera
            volumeRelativeDirPath.contains(Environment.DIRECTORY_DOWNLOADS) -> FileType.Source.Kind.Download
            else -> FileType.Source.Kind.OtherApp
        }
            .also {
                i { "Determined Source.Kind: ${it.name}" }
            }

    companion object {
        fun fetch(
            uri: Uri,
            contentResolver: ContentResolver
        ): MediaStoreColumnData? =
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
                        MediaStore.MediaColumns.IS_PENDING,
                    )
                )
                    ?.run {
                        MediaStoreColumnData(
                            rowId = get(0),
                            absPath = get(1),
                            volumeRelativeDirPath = get(2),
                            name = get(3),
                            dateTimeAdded = localDateTimeFromUnixTimeStamp(get(4).toLong()),
                            size = get(5).toLong(),
                            isPending = parseBoolean(get(6))
                        )
                            .also {
                                i { it.toString() }
                            }
                    }
            } catch (e: CursorIndexOutOfBoundsException) {
                emitDiscardedLog(e::toString)
                null
            }
    }
}