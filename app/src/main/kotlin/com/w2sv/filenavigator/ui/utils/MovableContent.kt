package com.w2sv.filenavigator.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

typealias ModifierReceivingComposable = @Composable (Modifier) -> Unit

@Composable
fun <P> rememberMovableContentOf(content: @Composable (P) -> Unit): @Composable (P) -> Unit =
    remember {
        movableContentOf(content)
    }