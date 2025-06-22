package com.w2sv.filenavigator.ui.screen.home

import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.NavigationDrawerScreenTopAppBar
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.drawer.NavigationDrawer
import com.w2sv.filenavigator.ui.designsystem.drawer.drawerRepelledAnimation
import com.w2sv.filenavigator.ui.designsystem.drawer.rememberDrawerRepelledAnimationState
import com.w2sv.filenavigator.ui.screen.home.components.movehistory.MoveHistoryCard
import com.w2sv.filenavigator.ui.screen.home.components.statusdisplay.NavigatorStatusCard
import com.w2sv.filenavigator.ui.util.ModifierReceivingComposable
import com.w2sv.filenavigator.ui.util.rememberMovableContentOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope(),
    viewModel: HomeScreenViewModel = hiltViewModel()
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
                    title = stringResource(id = R.string.app_name),
                    onNavigationIconClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        ) { paddingValues ->
            @SuppressLint("ConfigurationScreenWidthHeight")
            val sharedModifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .drawerRepelledAnimation(
                        state = rememberDrawerRepelledAnimationState(drawerState = drawerState),
                        animationBoxWidth = LocalConfiguration.current.screenWidthDp,
                        animationBoxHeight = LocalConfiguration.current.screenHeightDp
                    )

            val navigatorIsRunning by viewModel.navigatorIsRunning.collectAsStateWithLifecycle()

            val statusDisplayCard: ModifierReceivingComposable = rememberMovableContentOf {
                NavigatorStatusCard(navigatorIsRunning = navigatorIsRunning, modifier = it)
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
                .fillMaxWidth(0.4f)
        )
        moveHistoryCard(
            Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f)
        )
    }
}
