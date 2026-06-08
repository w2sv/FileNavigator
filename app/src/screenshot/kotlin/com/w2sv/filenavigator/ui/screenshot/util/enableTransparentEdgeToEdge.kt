package com.w2sv.filenavigator.ui.screenshot.util

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

internal fun ComponentActivity.enableTransparentEdgeToEdge() {
    val transparentSystemBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    enableEdgeToEdge(
        statusBarStyle = transparentSystemBarStyle,
        navigationBarStyle = transparentSystemBarStyle
    )
}
