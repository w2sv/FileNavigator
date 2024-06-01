package com.w2sv.filenavigator.ui.model

import androidx.compose.ui.graphics.Color
import com.w2sv.domain.model.FileTypeKind

/**
 * Returns previously cached Color.
 */
val FileTypeKind.color: Color
    get() = fileTypeColors.getValue(this)

private val fileTypeColors =
    FileTypeKind
        .values
        .associateWith { Color(it.colorInt) }
