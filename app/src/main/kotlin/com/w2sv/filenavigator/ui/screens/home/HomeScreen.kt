package com.w2sv.filenavigator.ui.screens.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.NavigationDrawerScreenTopAppBar
import com.w2sv.filenavigator.ui.designsystem.NavigationTransitions
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.drawer.NavigationDrawer
import com.w2sv.filenavigator.ui.designsystem.drawer.drawerRepelledAnimation
import com.w2sv.filenavigator.ui.designsystem.drawer.rememberDrawerRepelledAnimationState
import com.w2sv.filenavigator.ui.screens.home.components.movehistory.MoveHistoryCard
import com.w2sv.filenavigator.ui.screens.home.components.statusdisplay.StatusDisplayCard
import com.w2sv.filenavigator.ui.utils.ModifierReceivingComposable
import com.w2sv.filenavigator.ui.utils.rememberMovableContentOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Destination<RootGraph>(start = true, style = NavigationTransitions::class)
@Composable
fun HomeScreen(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    NavigationDrawer(state = drawerState) {
        Scaffold(
            snackbarHost = {
                AppSnackbarHost()
            },
            topBar = {
                NavigationDrawerScreenTopAppBar(
                    title = stringResource(id = com.w2sv.core.common.R.string.app_name),
                    onNavigationIconClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        ) { paddingValues ->
            val sharedModifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .drawerRepelledAnimation(
                        state = rememberDrawerRepelledAnimationState(drawerState = drawerState),
                        animationBoxWidth = LocalConfiguration.current.screenWidthDp,
                        animationBoxHeight = LocalConfiguration.current.screenHeightDp
                    )

            val statusDisplayCard: ModifierReceivingComposable = rememberMovableContentOf {
                StatusDisplayCard(it)
            }
            val moveHistoryCard: ModifierReceivingComposable = rememberMovableContentOf {
                MoveHistoryCard(it)
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
        statusDisplayCard(
            Modifier
                .fillMaxHeight(0.26f)
        )
        moveHistoryCard(Modifier.fillMaxHeight(0.7f))
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
                .fillMaxWidth(0.4f)
                .fillMaxHeight(0.65f)
        )
        moveHistoryCard(
            Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f)
        )
    }
}