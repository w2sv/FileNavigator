package com.w2sv.filenavigator.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T> ElementDividedColumn(
    elements: ImmutableList<T>,
    makeElement: @Composable ColumnScope.(T) -> Unit,
    modifier: Modifier = Modifier,
    makeDivider: @Composable ColumnScope.() -> Unit = { HorizontalDivider() },
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        elements.forEachIndexed { index, element ->
            makeElement(element)
            if (index != elements.lastIndex) {
                makeDivider()
            }
        }
    }
}