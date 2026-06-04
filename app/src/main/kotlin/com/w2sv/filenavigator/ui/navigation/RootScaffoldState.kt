package com.w2sv.filenavigator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class RootScaffoldState {
    var fab: (@Composable () -> Unit)? by mutableStateOf<(@Composable () -> Unit)?>(null)
}

@Composable
fun rememberRootScaffoldState(): RootScaffoldState =
    remember { RootScaffoldState() }
