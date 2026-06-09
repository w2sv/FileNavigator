package com.w2sv.navigator.notifications.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.w2sv.androidutils.res.isNightModeActiveCompat
import com.w2sv.domain.model.filetype.FileAndSourceType

internal fun FileAndSourceType.iconBitmap(context: Context, colored: Boolean = false): Bitmap? =
    context.drawableBitmap(
        drawable = iconRes,
        tint = when {
            colored -> fileType.colorInt
            context.resources.configuration.isNightModeActiveCompat -> null
            else -> Color.BLACK
        }
    )

internal fun Context.drawableBitmap(@DrawableRes drawable: Int, @ColorInt tint: Int? = null): Bitmap? =
    AppCompatResources.getDrawable(this, drawable)
        ?.apply { tint?.let { setTint(it) } }
        ?.toBitmap()
