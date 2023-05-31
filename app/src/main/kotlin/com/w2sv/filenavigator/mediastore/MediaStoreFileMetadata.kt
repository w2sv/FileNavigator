package com.w2sv.filenavigator.mediastore

import android.content.ContentResolver
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore.MediaColumns
import com.w2sv.kotlinutils.dateFromUnixTimestamp
import com.w2sv.kotlinutils.timeDelta
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import slimber.log.i
import java.util.Date
import java.util.concurrent.TimeUnit

@Parcelize
data class MediaStoreFileMetadata(
    val mediaType: MediaType,
    val mediaUri: Uri,
    val mediaId: String,
    val relativePath: String,
    val name: String,
    val dateAdded: Date,
    val size: String
) : Parcelable {

    @IgnoredOnParcel
    val isNewlyAdded: Boolean
        get() = timeDelta(
            dateAdded,
            Date(System.currentTimeMillis()),
            TimeUnit.SECONDS
        ) < 10

    fun pointsToSameContentAs(other: MediaStoreFileMetadata): Boolean =
        size == other.size && nonIncrementedNameWOExtension == other.nonIncrementedNameWOExtension    // TODO

    @IgnoredOnParcel
    val nonIncrementedNameWOExtension: String by lazy {
        name
            .substringBeforeLast(".")  // remove file extension
            .replace(Regex("\\(\\d+\\)$"), "")  // remove trailing file incrementation parentheses
    }

    companion object {

        fun fetch(
            uri: Uri,
            mediaType: MediaType,
            contentResolver: ContentResolver
        ): MediaStoreFileMetadata? =
            try {
                contentResolver.queryNonNullMediaStoreData(
                    uri,
                    arrayOf(
                        MediaColumns._ID,
                        MediaColumns.RELATIVE_PATH,
                        MediaColumns.DISPLAY_NAME,
                        MediaColumns.DATE_ADDED,
                        MediaColumns.SIZE,
                        MediaColumns.IS_PENDING
                    )
                )?.run {
                    i { "Raw mediaStoreColumns: ${toList()}" }

                    if (isPending(fileSize = get(4), isPendingFlag = get(5)))
                        null
                            .also {
                                i { "IsPending" }
                            }
                    else {
                        MediaStoreFileMetadata(
                            mediaType = mediaType,
                            mediaUri = uri,
                            mediaId = get(0),
                            relativePath = get(1),
                            name = get(2),
                            dateAdded = dateFromUnixTimestamp(get(3)),
                            size = get(4)
                        )
                    }
                }
            } catch (e: CursorIndexOutOfBoundsException) {
                i { e.toString() }
                null
            }
    }
}

private fun isPending(fileSize: String, isPendingFlag: String): Boolean =
    setOf("0", "null").contains(fileSize) || isPendingFlag != "0"