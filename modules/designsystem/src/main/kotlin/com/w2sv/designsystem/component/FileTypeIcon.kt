package com.w2sv.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.w2sv.designsystem.modelext.color
import com.w2sv.domain.model.filetype.FileType

@Composable
fun FileTypeIcon(fileType: FileType, modifier: Modifier = Modifier, tint: Color = fileType.color) {
    Icon(
        res = fileType.iconRes,
        modifier = modifier,
        tint = tint
    )
}
