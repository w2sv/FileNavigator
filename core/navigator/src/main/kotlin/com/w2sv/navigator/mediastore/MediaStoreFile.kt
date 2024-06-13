package com.w2sv.navigator.mediastore

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MediaStoreFile(
    val uri: Uri,
    val columnData: MediaStoreColumnData,
    val sha256: String
) : Parcelable {

    /**
     * @return true if this & other have the same URI (=> identical location) OR
     * if they have the identical content and file name (=> same file that has been moved to a different location).
     */
    fun isIdenticalFileAs(other: MediaStoreFile): Boolean =
        uri == other.uri || (sha256 == other.sha256 && columnData.name == other.columnData.name)
}