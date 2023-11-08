package com.w2sv.filenavigator.ui.utils

import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable

@Composable
fun <T> InBetweenSpaced(
    elements: List<T>,
    makeElement: @Composable (T) -> Unit,
    makeDivider: @Composable () -> Unit = { Divider() },
) {
    elements.forEachIndexed { index, element ->
        makeElement(element)
        if (index != elements.lastIndex) {
            makeDivider()
        }
    }
}