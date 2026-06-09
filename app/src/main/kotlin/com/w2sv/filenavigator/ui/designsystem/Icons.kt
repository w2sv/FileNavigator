package com.w2sv.filenavigator.ui.designsystem

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.filenavigator.ui.modelext.color

@Composable
fun FileTypeIcon(fileType: FileType, modifier: Modifier = Modifier, tint: Color = fileType.color) {
    Icon(
        res = fileType.iconRes,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun Icon(@DrawableRes res: Int, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        painter = painterResource(id = res),
        modifier = modifier,
        contentDescription = null,
        tint = tint
    )
}
