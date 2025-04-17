package com.w2sv.filenavigator.ui.modelext

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetWrappingFileType

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
        is PresetWrappingFileType<*> -> stringResource(presetFileType.labelRes)
        is CustomFileType -> name
    }
