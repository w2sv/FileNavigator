package com.w2sv.domain.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileAndSourceType(val fileType: FileType, val sourceType: SourceType) : Parcelable {
    @get:DrawableRes
    val iconRes: Int
        get() = combinedFileAndSourceTypeIconRes(fileType, sourceType)
}

@DrawableRes
fun combinedFileAndSourceTypeIconRes(fileType: FileType, sourceType: SourceType): Int =
    when (sourceType) {
        SourceType.Screenshot, SourceType.Camera -> fileType.iconRes
        else -> fileType.iconRes
    }