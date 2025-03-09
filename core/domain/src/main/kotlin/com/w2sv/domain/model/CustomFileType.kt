package com.w2sv.domain.model

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.w2sv.core.domain.R
import kotlin.random.Random
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomFileType(
    val name: String,
    override val fileExtensions: List<String>,
    @ColorInt override val colorInt: Int,
    override val ordinal: Int
) : NonMediaFileType(),
    Parcelable {

    @IgnoredOnParcel
    @DrawableRes
    override val iconRes: Int = R.drawable.ic_custom_file_type_24

    companion object {
        fun newEmpty(existingFileTypes: Collection<FileType>): CustomFileType =
            CustomFileType(
                name = "",
                fileExtensions = emptyList(),
                colorInt = randomColor(),
                ordinal = maxOf(MIN_ORDINAL, existingFileTypes.maxOf { it.ordinal } + 1)
            )

        private const val MIN_ORDINAL = 1_000
    }
}

@ColorInt
private fun randomColor(): Int =
    Color.rgb(
        Random.nextInt(256),
        Random.nextInt(256),
        Random.nextInt(256)
    )
