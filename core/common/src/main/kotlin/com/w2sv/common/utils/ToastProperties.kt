package com.w2sv.common.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.w2sv.androidutils.widget.showToast

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
@Retention(AnnotationRetention.SOURCE)
annotation class ToastDuration {
    companion object {
        const val DEFAULT = Toast.LENGTH_LONG
    }
}

sealed interface ToastMessage {
    @JvmInline
    value class String(val value: kotlin.String) : ToastMessage

    @JvmInline
    value class StringResId(@StringRes val value: Int) : ToastMessage
}

data class ToastProperties(
    val message: ToastMessage,
    @ToastDuration val duration: Int = ToastDuration.DEFAULT
) {
    constructor(message: String, @ToastDuration duration: Int = ToastDuration.DEFAULT) : this(
        message = ToastMessage.String(message),
        duration = duration
    )

    constructor(
        @StringRes message: Int,
        @ToastDuration duration: Int = ToastDuration.DEFAULT
    ) : this(
        message = ToastMessage.StringResId(message),
        duration = duration
    )
}

fun Context.showToast(properties: ToastProperties) {
    showToast(
        text = when (properties.message) {
            is ToastMessage.String -> properties.message.value
            is ToastMessage.StringResId -> getString(properties.message.value)
        },
        duration = properties.duration
    )
}