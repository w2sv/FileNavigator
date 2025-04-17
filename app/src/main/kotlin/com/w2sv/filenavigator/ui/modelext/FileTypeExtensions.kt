package com.w2sv.filenavigator.ui.modelext

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.PresetWrapper

/**
 * @return previously cached Color.
 */
val FileType.color: Color
    get() = colorCache.getOrPut(colorInt) { Color(colorInt) }

private val colorCache = mutableMapOf<Int, Color>()

@SuppressLint("ComposeUnstableReceiver")
@Composable
@ReadOnlyComposable
fun FileType.stringResource(): String =
    when (this) {
        is PresetWrapper<*> -> stringResource(presetFileType.labelRes)
        is CustomFileType -> name
    }
