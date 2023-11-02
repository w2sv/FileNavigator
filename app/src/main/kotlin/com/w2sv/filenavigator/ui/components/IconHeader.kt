package com.w2sv.filenavigator.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun IconHeader(
    @DrawableRes iconRes: Int,
    @StringRes headerRes: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.CenterEnd) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = color,
            )
        }
        Box(Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
            AppFontText(
                text = stringResource(id = headerRes),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = color,
            )
        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
}