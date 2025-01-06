package com.w2sv.domain.model

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.w2sv.core.domain.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileAndSourceType(val fileType: FileType, val sourceType: SourceType) : Parcelable {

    /**
     * @return
     * - Screenshot, Camera, Recording -> sourceTypeIcon
     * - else -> fileTypeIcon
     */
    @IgnoredOnParcel
    @get:DrawableRes
    val iconRes: Int by lazy {
        when (sourceType) {
            SourceType.Screenshot, SourceType.Camera, SourceType.Recording -> sourceType.iconRes
            else -> fileType.iconRes
        }
    }

    /**
     * @return
     * - Gif -> 'GIF'
     * - Photo -> 'Photo'
     * - Screenshot, Recording -> sourceTypeLabel
     * - Download -> '{fileTypeLabel} Download'
     * - else -> fileTypeLabel
     */
    fun label(context: Context, isGif: Boolean): String =
        when {
            isGif -> context.getString(R.string.gif)
            fileType is FileType.Image && sourceType == SourceType.Camera -> context.getString(R.string.photo)
            sourceType == SourceType.Screenshot || sourceType == SourceType.Recording -> context.getString(
                sourceType.labelRes
            )

            sourceType == SourceType.Download -> context.getString(
                R.string.file_type_download,
                context.getString(fileType.labelRes)
            )

            else -> context.getString(fileType.labelRes)
        }
}
