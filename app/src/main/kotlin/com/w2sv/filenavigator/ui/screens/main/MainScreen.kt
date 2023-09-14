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
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppSnackbar
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.AppTopBar
import com.w2sv.filenavigator.ui.components.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.components.drawer.NavigationDrawer
import com.w2sv.filenavigator.ui.screens.AppViewModel
import com.w2sv.filenavigator.ui.screens.main.components.ManageExternalStoragePermissionDialog
import com.w2sv.filenavigator.ui.screens.main.components.ConfigurationChangeConfirmationButtons
import com.w2sv.filenavigator.ui.screens.main.components.ToggleNavigatorButton
import com.w2sv.filenavigator.ui.screens.main.components.ToggleNavigatorButtonConfiguration
import com.w2sv.filenavigator.ui.screens.main.components.ToggleNavigatorButtonConfigurations
import com.w2sv.filenavigator.ui.screens.main.components.filetypeselection.FileTypeSelectionColumn
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.closeAnimated
import com.w2sv.filenavigator.ui.utils.openAnimated
import com.w2sv.filenavigator.ui.utils.visibilityPercentage
import com.w2sv.navigator.FileNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("NewApi")
@Composable
fun MainScreen(
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    mainScreenVM: MainScreenViewModel = viewModel(),
    appVM: AppViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    NavigationDrawer(drawerState) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { snackbarData ->
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
                drawerState = drawerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }

    mainScreenVM.showManageExternalStorageDialog.collectAsState()
        .apply {
            if (value) {
                ManageExternalStoragePermissionDialog(
                    onConfirmation = {
                        goToManageExternalStorageSettings(
                            context
                        )
                    },
                    onDismissRequest = {
                        mainScreenVM.saveShowedManageExternalStorageRational()
                    }
                )
            }
        }

    BackHandler {
        when (drawerState.currentValue) {
            DrawerValue.Closed -> appVM.onBackPress(context)
            DrawerValue.Open -> scope.launch {
                drawerState.closeAnimated()
            }
        }
    }
}

@Composable
internal fun ScaffoldContent(
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    mainScreenVM: MainScreenViewModel = viewModel()
) {
    val maxDrawerWidthPx = with(LocalDensity.current) { DrawerDefaults.MaximumDrawerWidth.toPx() }

    val drawerVisibilityPercentage by remember {
        drawerState.visibilityPercentage(maxWidthPx = maxDrawerWidthPx)
    }
    val drawerVisibilityPercentageInverse by remember {
        derivedStateOf {
            1 - drawerVisibilityPercentage
        }
    }

    val drawerVisibilityPercentageAngle by remember {
        derivedStateOf {
            180 * drawerVisibilityPercentage
        }
    }

//    val permissionState =
//        rememberMultiplePermissionsState(permissions = buildList {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                add(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }) { isGranted ->
//            if (isGranted.values.all { it }) {
////                startNavigator()
//            }
//        }

    val toggleNavigatorButtonConfigurations = remember {
        ToggleNavigatorButtonConfigurations(
            startNavigator = ToggleNavigatorButtonConfiguration(
                AppColor.success,
                R.drawable.ic_start_24,
                R.string.start_navigator
            ) { FileNavigator.start(context) },
            stopNavigator = ToggleNavigatorButtonConfiguration(
                AppColor.error,
                R.drawable.ic_stop_24,
                R.string.stop_navigator
            ) { FileNavigator.stop(context) }
        )
    }

    Surface(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer(
                    scaleX = drawerVisibilityPercentageInverse,
                    scaleY = drawerVisibilityPercentageInverse,
                    translationX = LocalConfiguration.current.screenWidthDp * drawerVisibilityPercentage,
                    translationY = LocalConfiguration.current.screenHeightDp * drawerVisibilityPercentage,
                    rotationY = drawerVisibilityPercentageAngle,
                    rotationZ = drawerVisibilityPercentageAngle
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
                    targetState = mainScreenVM.navigatorUIState.configuration.statesDissimilar.collectAsState().value,
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
                        ConfigurationChangeConfirmationButtons()
                    } else {
                        ToggleNavigatorButton(
                            configuration = when (mainScreenVM.navigatorUIState.isRunning.collectAsState().value) {
                                true -> toggleNavigatorButtonConfigurations.stopNavigator
                                false -> toggleNavigatorButtonConfigurations.startNavigator
                            },
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