package com.w2sv.filenavigator.ui.modelext

import androidx.compose.ui.graphics.Color
import com.w2sv.domain.model.filetype.FileType

/**
 * @return Compose color corresponding to the defined [FileType.colorInt].
 */
val FileType.color: Color
    get() = Color(colorInt)
