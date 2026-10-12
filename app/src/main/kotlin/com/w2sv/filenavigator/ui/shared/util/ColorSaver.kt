package com.w2sv.filenavigator.ui.shared.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

internal fun colorSaver(): Saver<Color, Int> =
    Saver(save = { it.toArgb() }, restore = { Color(it) })
