package com.w2sv.domain.model.filetype

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.VisibleForTesting
import com.w2sv.core.common.R
import kotlin.random.Random
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomFileType(
    val name: String,
    override val fileExtensions: List<String>,
    @ColorInt override val colorInt: Int,
    override val ordinal: Int
) : StaticFileType.NonMedia,
    FileType,
    Parcelable {

    @IgnoredOnParcel
    @DrawableRes
    override val iconRes: Int = R.drawable.ic_custom_file_type_24

    override fun label(context: Context): String =
        name

    companion object {
        fun newEmpty(existingFileTypes: Collection<FileType>): CustomFileType =
            CustomFileType(
                name = "",
                fileExtensions = emptyList(),
                colorInt = randomColor(),
                ordinal = maxOf(MIN_ORDINAL, existingFileTypes.maxOfOrNull { it.ordinal }?.let { it + 1 } ?: MIN_ORDINAL)
            )

        @VisibleForTesting
        internal const val MIN_ORDINAL = 1_000
    }
}

@ColorInt
private fun randomColor(): Int =
    Color.rgb(
        Random.Default.nextInt(256),
        Random.Default.nextInt(256),
        Random.Default.nextInt(256)
    )
