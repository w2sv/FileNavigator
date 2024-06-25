package com.w2sv.navigator.mediastore

import android.content.ContentResolver
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import com.w2sv.androidutils.database.getBooleanOrThrow
import com.w2sv.androidutils.database.getLongOrThrow
import com.w2sv.androidutils.database.getStringOrThrow
import com.w2sv.androidutils.database.query
import com.w2sv.common.utils.MediaUri
import com.w2sv.domain.model.SourceType
import com.w2sv.kotlinutils.time.localDateTimeFromMilliSecondsUnixTimestamp
import com.w2sv.navigator.shared.emitDiscardedLog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import slimber.log.i
import java.io.File
import java.time.LocalDateTime

/**
 * @param volumeRelativeDirPath Relative dir path from the storage volume, e.g. "Documents/", "DCIM/Camera/".
 */
@Parcelize
internal data class MediaStoreColumnData(
    val rowId: String,
    val absPath: String,
    val volumeRelativeDirPath: String,
    val name: String,
    val dateTimeAdded: LocalDateTime,
    val size: Long,
    val isPending: Boolean,
    val isTrashed: Boolean
) : Parcelable {

    @IgnoredOnParcel
    val fileExtension: String by lazy {
        name.substringAfterLast(".")
    }

//    @IgnoredOnParcel
//    val nonIncrementedNameWOExtension: String by lazy {
//        name
//            .substringBeforeLast(".")  // remove file extension
//            .replace(  // remove trailing file incrementation parentheses
//                Regex("\\(\\d+\\)$"),
//                ""
//            )
//    }

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

    fun getSourceType(): SourceType =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && volumeRelativeDirPath.contains(
                Environment.DIRECTORY_RECORDINGS
            ) || volumeRelativeDirPath.contains("Recordings") -> SourceType.Recording
            // NOTE: Don't change the order of the Screenshot and Camera branches, as Environment.DIRECTORY_SCREENSHOTS
            // may be a child dir of Environment.DIRECTORY_DCIM
            volumeRelativeDirPath.contains(Environment.DIRECTORY_SCREENSHOTS) -> SourceType.Screenshot
            volumeRelativeDirPath.contains(Environment.DIRECTORY_DCIM) -> SourceType.Camera
            volumeRelativeDirPath.contains(Environment.DIRECTORY_DOWNLOADS) -> SourceType.Download
            else -> SourceType.OtherApp
        }
            .also {
                i { "Determined Source.Kind: ${it.name}" }
            }

    companion object {
        fun fetch(
            mediaUri: MediaUri,
            contentResolver: ContentResolver
        ): MediaStoreColumnData? =
            try {
                contentResolver.query(
                    uri = mediaUri.uri,
                    columns = arrayOf(
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DATA,
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.DATE_ADDED,
                        MediaStore.MediaColumns.SIZE,
                        MediaStore.MediaColumns.IS_PENDING,
                        MediaStore.MediaColumns.IS_TRASHED,
                    )
                ) {
                    MediaStoreColumnData(
                        rowId = it.getStringOrThrow(MediaStore.MediaColumns._ID),
                        absPath = it.getStringOrThrow(MediaStore.MediaColumns.DATA),
                        volumeRelativeDirPath = it.getStringOrThrow(MediaStore.MediaColumns.RELATIVE_PATH),
                        name = it.getStringOrThrow(MediaStore.MediaColumns.DISPLAY_NAME),
                        dateTimeAdded = localDateTimeFromMilliSecondsUnixTimestamp(
                            it.getLongOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                        ),
                        size = it.getLongOrThrow(MediaStore.MediaColumns.SIZE),
                        isPending = it.getBooleanOrThrow(MediaStore.MediaColumns.IS_PENDING),
                        isTrashed = it.getBooleanOrThrow(MediaStore.MediaColumns.IS_TRASHED),
                    )
                        .also {
                            i { it.toString() }
                        }
                }
            } catch (e: Exception) {
                emitDiscardedLog(e::toString)
                null
            }
    }
}