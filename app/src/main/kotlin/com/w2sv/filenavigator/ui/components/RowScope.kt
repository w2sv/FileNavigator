package com.w2sv.filenavigator.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RowScope.WeightedBox(
    weight: Float,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.CenterStart,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.weight(weight), contentAlignment = contentAlignment) {
        content()
    }
}

@Composable
fun RowScope.RightAligned(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Spacer(modifier = modifier.weight(1f))
    content()
}