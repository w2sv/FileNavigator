package com.w2sv.navigator.notifications.managers

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.w2sv.domain.model.FileAndSourceType

internal fun FileAndSourceType.coloredIconBitmap(context: Context): Bitmap? =
    AppCompatResources.getDrawable(context, iconRes)
        ?.apply { setTint(fileType.colorInt) }
        ?.toBitmap()
