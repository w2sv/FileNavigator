package com.w2sv.filenavigator.mediastore

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @param uri The MediaStore URI.
 */
@Parcelize
data class MoveFile(
    val uri: Uri,
    val type: FileType,
    val defaultTargetDir: FileType.Source.DefaultTargetDir,
    val data: MediaStoreFileData
) : Parcelable