package com.w2sv.filenavigator.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NavGraph(navigator: Navigator) {
    val rootScaffoldState = rememberRootScaffoldState()

    RootScaffold(
        navigator = navigator,
        rootScaffoldState = rootScaffoldState
    ) { paddingValues ->
        NavContent(
            navigator = navigator,
            rootScaffoldState = rootScaffoldState,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
