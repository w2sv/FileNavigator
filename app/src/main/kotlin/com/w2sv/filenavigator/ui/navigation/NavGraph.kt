package com.w2sv.filenavigator.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.w2sv.filenavigator.ui.sharedstate.AppPermissionsState

@Composable
fun NavGraph(navigator: Navigator, permissionsState: AppPermissionsState) {
    val rootScaffoldState = rememberRootScaffoldState()

    RootScaffold(
        navigator = navigator,
        rootScaffoldState = rootScaffoldState
    ) { paddingValues ->
        NavContent(
            navigator = navigator,
            rootScaffoldState = rootScaffoldState,
            permissionsState = permissionsState,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
