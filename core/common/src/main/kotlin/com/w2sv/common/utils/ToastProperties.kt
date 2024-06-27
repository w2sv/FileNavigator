package com.w2sv.common.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.w2sv.androidutils.widget.showToast

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
@Retention(AnnotationRetention.SOURCE)
private annotation class ToastDuration {
    companion object {
        const val DEFAULT = Toast.LENGTH_LONG
    }
}

@Suppress("DataClassPrivateConstructor")
data class ToastProperties private constructor(
    val message: Message,
    @ToastDuration val duration: Int = ToastDuration.DEFAULT
) {
    constructor(message: String, @ToastDuration duration: Int = ToastDuration.DEFAULT) : this(
        message = Message.String(message),
        duration = duration
    )

    constructor(
        @StringRes message: Int,
        @ToastDuration duration: Int = ToastDuration.DEFAULT
    ) : this(
        message = Message.StringResId(message),
        duration = duration
    )

    sealed interface Message {
        @JvmInline
        value class String(val value: kotlin.String) : Message

        @JvmInline
        value class StringResId(@StringRes val value: Int) : Message
    }
}

fun Context.showToast(properties: ToastProperties) {
    showToast(
        text = when (properties.message) {
            is ToastProperties.Message.String -> properties.message.value
            is ToastProperties.Message.StringResId -> getString(properties.message.value)
        },
        duration = properties.duration
    )
}