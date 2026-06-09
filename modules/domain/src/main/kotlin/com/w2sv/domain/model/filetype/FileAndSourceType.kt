package com.w2sv.domain.model.filetype

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.w2sv.modules.resources.R
import kotlin.collections.listOf
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Pairing of a matched [FileType] with the source category that produced it.
 *
 * A file type can be valid for multiple sources, for example image downloads
 * and camera photos. This value captures the concrete combination used for
 * labels, icons and source-specific navigation settings.
 */
@Parcelize
data class FileAndSourceType(val fileType: FileType, val sourceType: SourceType) : Parcelable {

    @IgnoredOnParcel
    @get:DrawableRes
    val iconRes: Int by lazy {
        when {
            sourceType in listOf(SourceType.Screenshot, SourceType.Camera, SourceType.Recording) -> sourceType.iconRes
            else -> fileType.iconRes
        }
    }

    /**
     * @return
     * - Gif -> 'GIF'
     * - SourceType.Camera -> 'Photo' or 'Video"
     * - SourceType in (Screenshot, Recording) -> sourceTypeLabel
     * - else -> fileTypeLabel
     */
    fun label(context: Context, isGif: Boolean): String =
        when {
            isGif -> context.getString(R.string.gif)
            sourceType == SourceType.Camera -> context.getString(
                when (fileType.presetTypeOrNull) {
                    PresetFileType.Image -> R.string.photo
                    PresetFileType.Video -> R.string.video
                    else -> error(
                        "file type should be PresetFileType.Image or PresetFileType.Video but was ${fileType.presetTypeOrNull}"
                    )
                }
            )

            sourceType in listOf(SourceType.Screenshot, SourceType.Recording) -> context.getString(
                sourceType.labelRes
            )

            else -> fileType.name(context)
        }
}
