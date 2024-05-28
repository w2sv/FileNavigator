package com.w2sv.common.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

fun Context.startActivityWithActivityNotFoundExceptionHandling(
    intent: Intent,
    onActivityNotFoundException: (Context) -> Unit
) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        onActivityNotFoundException(this)
    }
}