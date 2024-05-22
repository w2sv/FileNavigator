package com.w2sv.filenavigator.ui.designsystem.drawer

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.ui.model.color

@Composable
fun FileTypeIcon(fileType: FileType, modifier: Modifier = Modifier, tint: Color = fileType.color) {
    Icon(
        painter = painterResource(id = fileType.iconRes),
        contentDescription = null,
        modifier = modifier
            .size(34.dp),
        tint = tint
    )
}