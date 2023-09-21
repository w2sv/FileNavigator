package com.w2sv.filenavigator.ui.utils

import androidx.compose.runtime.Composable

@Composable
fun <T> InBetweenSpaced(
    elements: List<T>,
    makeElement: @Composable (T) -> Unit,
    spacer: @Composable () -> Unit,
) {
    elements.forEachIndexed { index, element ->
        makeElement(element)
        if (index != elements.lastIndex) {
            spacer()
        }
    }
}