package com.w2sv.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun Icon(@DrawableRes res: Int, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        painter = painterResource(id = res),
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}
