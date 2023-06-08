package com.w2sv.filenavigator.mediastore

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @param uri The MediaStore URI.
 */
@Parcelize
data class MediaStoreFile(
    val uri: Uri,
    val type: MediaType,
    val data: MediaStoreFileData
) : Parcelable