package com.w2sv.navigator.observing.model

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
import com.w2sv.kotlinutils.time.localDateTimeFromSecondsUnixTimestamp
import com.w2sv.navigator.shared.emitDiscardedLog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import slimber.log.i
import java.io.File
import java.time.LocalDateTime

/**
 * @param volumeRelativeDirPath Relative to the storage volume, e.g. "Documents/", "DCIM/Camera/".
 */
@Parcelize
internal data class MediaStoreFileData(
    val rowId: String,
    val absPath: String,
    val volumeRelativeDirPath: String,
    val dateTimeAdded: LocalDateTime,
    val size: Long,
    val isPending: Boolean,
    val isTrashed: Boolean
) : Parcelable {

    @IgnoredOnParcel
    val name: String by lazy {
        absPath.substringAfterLast(File.separator)
    }

    /**
     * Equals empty string if no extension present (=> directory).
     */
    @IgnoredOnParcel
    val extension: String by lazy {
        name.substringAfterLast(".", "")
    }

    @IgnoredOnParcel
    val parentDirName: String by lazy {
        volumeRelativeDirPath
            .removeSuffix(File.separator)
            .substringAfterLast(File.separator)
    }

    @IgnoredOnParcel
    val file: File by lazy {
        File(absPath)
    }

    val fileExists: Boolean
        get() = file.exists()

    fun sourceType(): SourceType =
        when {
            volumeRelativeDirPath.contains(directoryRecordingsCompat) -> SourceType.Recording
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
        private val queryColumns = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.IS_PENDING,
            MediaStore.MediaColumns.IS_TRASHED,
        )

        fun queryFor(
            mediaUri: MediaUri,
            contentResolver: ContentResolver
        ): MediaStoreFileData? =
            try {
                contentResolver.query(
                    uri = mediaUri.uri,
                    columns = queryColumns
                ) {
                    MediaStoreFileData(
                        rowId = it.getStringOrThrow(MediaStore.MediaColumns._ID),
                        absPath = it.getStringOrThrow(MediaStore.MediaColumns.DATA),
                        volumeRelativeDirPath = it.getStringOrThrow(MediaStore.MediaColumns.RELATIVE_PATH),
                        dateTimeAdded = localDateTimeFromSecondsUnixTimestamp(
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

private val directoryRecordingsCompat: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Environment.DIRECTORY_RECORDINGS else "Recordings"