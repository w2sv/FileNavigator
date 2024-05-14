package com.w2sv.filenavigator.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ColumnScope.OptionalAnimatedVisibility(
    visible: (() -> Boolean)?,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandVertically(),
    exit: ExitTransition = fadeOut() + shrinkVertically(),
    label: String = "AnimatedVisibility",
    content: @Composable (ColumnScope.() -> Unit)
) {
    visible?.let {
        AnimatedVisibility(
            visible = it(),
            modifier = modifier,
            enter = enter,
            exit = exit,
            label = label
        ) {
            content()
        }
    } ?: content()
}