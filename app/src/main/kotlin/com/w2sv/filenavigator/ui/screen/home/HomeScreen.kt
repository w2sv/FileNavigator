package com.w2sv.filenavigator.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.composed.material3.extensions.rememberVisibilityProgress
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.NavigationDrawerScreenTopAppBar
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.drawer.HomeScreenNavigationDrawer
import com.w2sv.filenavigator.ui.designsystem.drawer.drawerDisplaced
import com.w2sv.filenavigator.ui.screen.home.components.movehistory.MoveHistoryCard
import com.w2sv.filenavigator.ui.screen.home.components.statusdisplay.NavigatorStatusCard
import com.w2sv.filenavigator.ui.util.ModifierReceivingComposable
import com.w2sv.filenavigator.ui.util.PreviewOf
import com.w2sv.filenavigator.ui.util.rememberMovableContentOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = hiltViewModel()) {
    val navigatorIsRunning by viewModel.navigatorIsRunning.collectAsStateWithLifecycle()
    val moveHistoryState = MoveHistoryState.remember(viewModel)

    HomeScreen(navigatorIsRunning = navigatorIsRunning, moveHistoryState = moveHistoryState)
}

@Preview
@Composable
private fun HomeScreenPrev() {
    PreviewOf {
        HomeScreen(
            navigatorIsRunning = true,
            moveHistoryState = MoveHistoryState(
                history = persistentListOf(),
                deleteAll = {},
                deleteEntry = {}
            )
        )
    }
}

@Composable
private fun HomeScreen(
    navigatorIsRunning: Boolean,
    moveHistoryState: MoveHistoryState,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {
    val scope = rememberCoroutineScope()
    var contentWidthPx by remember { mutableIntStateOf(0) }
    val drawerVisibilityProgress by drawerState.rememberVisibilityProgress()

    HomeScreenNavigationDrawer(state = drawerState) {
        Scaffold(
            snackbarHost = { AppSnackbarHost() },
            topBar = {
                NavigationDrawerScreenTopAppBar(
                    title = stringResource(id = R.string.app_name),
                    onNavigationIconClick = { scope.launch { drawerState.open() } }
                )
            },
            modifier = Modifier.onSizeChanged { contentWidthPx = it.width }
        ) { paddingValues ->
            val sharedModifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .drawerDisplaced(
                        visibilityProgress = drawerVisibilityProgress,
                        animationBoxWidthPx = contentWidthPx.toFloat()
                    )

            val statusDisplayCard: ModifierReceivingComposable = rememberMovableContentOf {
                NavigatorStatusCard(navigatorIsRunning = navigatorIsRunning, modifier = it)
            }
            val moveHistoryCard: ModifierReceivingComposable = rememberMovableContentOf {
                MoveHistoryCard(state = moveHistoryState, modifier = it)
            }

            when (isPortraitModeActive) {
                true -> PortraitMode(
                    statusDisplayCard = statusDisplayCard,
                    moveHistoryCard = moveHistoryCard,
                    modifier = sharedModifier
                )

                false -> LandscapeMode(
                    statusDisplayCard = statusDisplayCard,
                    moveHistoryCard = moveHistoryCard,
                    modifier = sharedModifier
                )
            }
        }
    }
}

@Composable
private fun PortraitMode(
    statusDisplayCard: ModifierReceivingComposable,
    moveHistoryCard: ModifierReceivingComposable,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = Padding.defaultHorizontal),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        statusDisplayCard(Modifier)
        moveHistoryCard(Modifier.fillMaxHeight(0.8f))
    }
}

@Composable
private fun LandscapeMode(
    statusDisplayCard: ModifierReceivingComposable,
    moveHistoryCard: ModifierReceivingComposable,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        statusDisplayCard(
            Modifier
                .fillMaxWidth(0.45f)
        )
        moveHistoryCard(
            Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f)
        )
    }
}
