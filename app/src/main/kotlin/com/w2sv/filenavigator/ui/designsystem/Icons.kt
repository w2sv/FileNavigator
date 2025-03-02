package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.ui.modelext.color

object IconSize {
    val Default = 24.dp
    val Big = 28.dp
}

@Composable
fun FileTypeIcon(
    fileType: FileType,
    modifier: Modifier = Modifier,
    tint: Color = fileType.color
) {
    Icon(
        painter = painterResource(id = fileType.iconRes),
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}
