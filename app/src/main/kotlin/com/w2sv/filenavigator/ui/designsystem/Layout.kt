package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalLayoutApi::class)
@Composable
inline fun SystemBarsIgnoringVisibilityPaddedColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility))
        content()
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBarsIgnoringVisibility))
    }
}

val emptyInsets = WindowInsets(0, 0, 0, 0)

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