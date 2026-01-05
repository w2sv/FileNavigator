package com.w2sv.common.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent

// TODO AndroidUtils
inline fun <reified T> intent(context: Context): Intent =
    Intent(context, T::class.java)

/**
 * @see [Intent.makeRestartActivityTask]
 */
inline fun <reified T> restartActivityTaskIntent(context: Context): Intent =
    Intent.makeRestartActivityTask(
        ComponentName(
            context,
            T::class.java
        )
    )
