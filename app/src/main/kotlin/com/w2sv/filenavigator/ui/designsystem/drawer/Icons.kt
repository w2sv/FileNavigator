package com.w2sv.filenavigator.ui.designsystem.drawer

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.w2sv.domain.model.FileTypeKind
import com.w2sv.filenavigator.ui.model.color

@Composable
fun FileTypeIcon(fileType: FileTypeKind, modifier: Modifier = Modifier, tint: Color = fileType.color) {
    Icon(
        painter = painterResource(id = fileType.iconRes),
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}