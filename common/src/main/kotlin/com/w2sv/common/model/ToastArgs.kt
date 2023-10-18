package com.w2sv.common.model

import android.widget.Toast
import androidx.annotation.IntDef

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
@Retention(AnnotationRetention.SOURCE)
annotation class ToastDuration

data class ToastArgs(
    val message: String,
    @ToastDuration val duration: Int = Toast.LENGTH_SHORT
)