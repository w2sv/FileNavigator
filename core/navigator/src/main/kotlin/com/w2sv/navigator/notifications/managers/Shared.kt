package com.w2sv.navigator.notifications.managers

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.w2sv.domain.model.FileAndSourceType

internal fun FileAndSourceType.coloredIconBitmap(context: Context): Bitmap? =
    context.drawableBitmap(iconRes, fileType.colorInt)

internal fun Context.drawableBitmap(
    @DrawableRes drawable: Int,
    @ColorInt tint: Int? = null
): Bitmap? =
    AppCompatResources.getDrawable(this, drawable)
        ?.apply { tint?.let { setTint(it) } }
        ?.toBitmap()
