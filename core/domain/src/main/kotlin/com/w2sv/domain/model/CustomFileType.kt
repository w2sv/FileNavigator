package com.w2sv.domain.model

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import com.w2sv.core.domain.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomFileType(
    val name: String,
    override val fileExtensions: List<String>,
    override val ordinal: Int
) : NonMediaFileType(),
    Parcelable {

    @IgnoredOnParcel
    @ColorInt
    override val colorInt: Int = -13590298

    @IgnoredOnParcel
    @DrawableRes
    override val iconRes: Int = R.drawable.ic_custom_file_type_24

    companion object {
        @IntRange(from = 1000)
        fun ordinal(existingFileTypes: Collection<FileType>): Int =
            maxOf(1000, existingFileTypes.maxOf { it.ordinal } + 1)
    }
}
