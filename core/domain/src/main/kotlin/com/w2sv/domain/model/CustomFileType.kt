package com.w2sv.domain.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
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
    override val colorInt: Int = -16252812

    @IgnoredOnParcel
    @DrawableRes
    override val iconRes: Int = R.drawable.ic_custom_file_type_24
}
