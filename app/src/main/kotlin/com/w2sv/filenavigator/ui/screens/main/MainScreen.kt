package com.w2sv.filenavigator.ui.screens.main

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.filenavigator.ui.components.AppSnackbar
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.closeAnimated
import com.w2sv.filenavigator.ui.components.openAnimated
import com.w2sv.filenavigator.ui.components.AppTopBar
import com.w2sv.filenavigator.ui.screens.main.components.FileTypeSelectionColumn
import com.w2sv.filenavigator.ui.screens.main.components.ManageExternalStoragePermissionDialog
import com.w2sv.filenavigator.ui.screens.main.components.NavigationDrawer
import com.w2sv.filenavigator.ui.screens.main.components.NavigatorConfigurationButtons
import com.w2sv.filenavigator.ui.screens.main.components.StartNavigatorButton
import com.w2sv.filenavigator.ui.screens.main.components.offsetFraction
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val closeDrawer: () -> Unit = {
        scope.launch {
            drawerState.closeAnimated()
        }
    }

    NavigationDrawer(drawerState, closeDrawer) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(mainScreenViewModel.snackbarHostState) { snackbarData ->
                    AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
                }
            },
            topBar = {
                AppTopBar(
                    onNavigationIconClick = {
                        scope.launch {
                            drawerState.openAnimated()
                        }
                    }
                )
            }
        ) { paddingValues ->
            ScaffoldContent(
                drawerState = drawerState, modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }

    EventualManageExternalStoragePermissionRational()

    BackHandler {
        when (drawerState.currentValue) {
            DrawerValue.Closed -> mainScreenViewModel.onBackPress(context)
            DrawerValue.Open -> closeDrawer()
        }
    }
}

@Composable
internal fun ScaffoldContent(
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val minValue = with(LocalDensity.current) { 360.dp.toPx() }

    val drawerProgress by remember {
        drawerState.offsetFraction(minValue.toInt())
    }

    Surface(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer(
                    scaleX = 1 - drawerProgress,
                    translationX = LocalConfiguration.current.screenWidthDp * drawerProgress,
                    alpha = 1 - drawerProgress
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(modifier = Modifier.weight(0.075f))

            Box(modifier = Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
                FileTypeSelectionColumn(Modifier.fillMaxHeight())
            }

            Box(modifier = Modifier.weight(0.25f), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    contentAlignment = Alignment.Center,
                    targetState = mainScreenViewModel.unconfirmedNavigatorConfiguration.statesDissimilar.collectAsState().value,
                    transitionSpec = {
                        (slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = DefaultAnimationDuration
                            ),
                            initialOffsetX = { -it }
                        ) + scaleIn(
                            animationSpec = tween(
                                durationMillis = DefaultAnimationDuration
                            )
                        )).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(
                                    durationMillis = DefaultAnimationDuration
                                )
                            ) + scaleOut(
                                animationSpec = tween(
                                    durationMillis = DefaultAnimationDuration
                                )
                            )
                        )
                    },
                    label = ""
                ) {
                    if (it) {
                        NavigatorConfigurationButtons()
                    } else {
                        StartNavigatorButton(
                            modifier = Modifier
                                .width(220.dp)
                                .height(70.dp)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
internal fun EventualManageExternalStoragePermissionRational(
    context: Context = LocalContext.current,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    var showManageExternalStorageRational by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                val onDismissRequest: () -> Unit = {
                    mainScreenViewModel.saveShowedManageExternalStorageRational()
                    value = false
                }
                ManageExternalStoragePermissionDialog(
                    onConfirmation = {
                        onDismissRequest()
                        goToManageExternalStorageSettings(
                            context
                        )
                    },
                    onDismissRequest = onDismissRequest
                )
            }
        }

    if (!mainScreenViewModel.anyStorageAccessGranted.collectAsState().value && !mainScreenViewModel.showedManageExternalStorageRational.collectAsState(
            initial = false
        ).value
    ) {
        LaunchedEffect(
            key1 = Unit,
            block = {
                delay(1000L)
                showManageExternalStorageRational = true
            }
        )
    }
}