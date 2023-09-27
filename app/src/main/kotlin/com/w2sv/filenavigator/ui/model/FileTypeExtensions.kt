package com.w2sv.filenavigator.ui.model

import androidx.compose.ui.graphics.Color
import com.w2sv.data.model.FileType

private val fileTypeColors = FileType.values.associateWith { Color(it.colorLong) }

val FileType.color: Color
    get() = fileTypeColors.getValue(this)