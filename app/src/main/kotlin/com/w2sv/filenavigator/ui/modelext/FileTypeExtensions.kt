package com.w2sv.filenavigator.ui.modelext

import androidx.compose.ui.graphics.Color
import com.w2sv.domain.model.filetype.FileType

/**
 * @return previously cached Color.
 */
val FileType.color: Color
    get() = colorCache.getOrPut(colorInt) { Color(colorInt) }

private val colorCache = mutableMapOf<Int, Color>()
