package com.w2sv.navigator.mediastore

import android.os.Parcelable
import com.w2sv.common.utils.MediaUri
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MediaStoreFile(
    val mediaUri: MediaUri,
    val columnData: MediaStoreData,
    val sha256: String
) : Parcelable {

    /**
     * @return true if this & other have the same URI (=> identical location) OR
     * if they have the identical content and file name (=> same file that has been moved to a different location).
     */
    fun isIdenticalFileAs(other: MediaStoreFile): Boolean =
        mediaUri == other.mediaUri || (sha256 == other.sha256 && columnData.name == other.columnData.name)
}