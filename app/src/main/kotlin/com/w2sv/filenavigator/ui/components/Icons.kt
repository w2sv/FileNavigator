package com.w2sv.filenavigator.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.R

@Composable
fun InfoIcon(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    Icon(
        painter = painterResource(id = R.drawable.ic_info_24),
        modifier = modifier.size(size),
        contentDescription = null
    )
}