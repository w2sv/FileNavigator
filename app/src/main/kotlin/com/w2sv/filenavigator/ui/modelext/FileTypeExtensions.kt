package com.w2sv.filenavigator.ui.modelext

import androidx.compose.ui.graphics.Color
import com.w2sv.domain.model.FileType

/**
 * Returns previously cached Color.
 */
val FileType.color: Color
    get() = fileTypeColors.getValue(this)

private val fileTypeColors =
    FileType
        .values
        .associateWith { Color(it.colorInt) }
