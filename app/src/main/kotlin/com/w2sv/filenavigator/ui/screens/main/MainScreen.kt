package com.w2sv.filenavigator.ui.screens.main

import android.Manifest
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
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.AppSnackbar
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.AppTopBar
import com.w2sv.filenavigator.ui.components.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.components.PermissionCard
import com.w2sv.filenavigator.ui.components.PermissionCardProperties
import com.w2sv.filenavigator.ui.components.drawer.NavigationDrawer
import com.w2sv.filenavigator.ui.screens.AppViewModel
import com.w2sv.filenavigator.ui.screens.main.components.ConfigurationChangeConfirmationButtons
import com.w2sv.filenavigator.ui.screens.main.components.ToggleNavigatorButton
import com.w2sv.filenavigator.ui.screens.main.components.ToggleNavigatorButtonConfiguration
import com.w2sv.filenavigator.ui.screens.main.components.ToggleNavigatorButtonConfigurations
import com.w2sv.filenavigator.ui.screens.main.components.filetypeselection.FileTypeSelectionColumn
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.InBetweenSpaced
import com.w2sv.filenavigator.ui.utils.extensions.closeAnimated
import com.w2sv.filenavigator.ui.utils.extensions.launchPermissionRequest
import com.w2sv.filenavigator.ui.utils.extensions.openAnimated
import com.w2sv.filenavigator.ui.utils.extensions.visibilityPercentage
import com.w2sv.navigator.FileNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
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

    val postNotificationsPermissionState =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) {
            mainScreenVM.savePostNotificationsPermissionRequested()
        }

    val anyStorageAccessGranted by mainScreenVM.storageAccessState.anyAccessGranted.collectAsState()

    val permissionCardProperties = remember(
        key1 = postNotificationsPermissionState.status.isGranted,
        key2 = anyStorageAccessGranted
    ) {
        buildList {
            if (!postNotificationsPermissionState.status.isGranted) {
                add(
                    PermissionCardProperties(
                        iconRes = R.drawable.ic_notifications_24,
                        textRes = R.string.post_notifications_permission_rational,
                        onGrantButtonClick = {
                            postNotificationsPermissionState.launchPermissionRequest(
                                launchedBefore = mainScreenVM.postNotificationsPermissionRequested.value,
                                onBlocked = {
                                    goToAppSettings(
                                        context
                                    )
                                }
                            )
                        }
                    )
                )
            }
            if (!anyStorageAccessGranted) {
                add(
                    PermissionCardProperties(
                        iconRes = R.drawable.ic_storage_24,
                        textRes = R.string.manage_external_storage_permission_rational,
                        onGrantButtonClick = {
                            goToManageExternalStorageSettings(context)
                        }
                    )
                )
            }
        }
    }

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
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val sharedModifier =
                    Modifier
                        .fillMaxSize()
                        .animateDrawerProgressionBased(drawerState)

                AnimatedContent(targetState = permissionCardProperties.isEmpty(), label = "") {
                    if (it) {
                        MainContent(
                            modifier = sharedModifier.padding(horizontal = 14.dp)
                        )
                    } else {
                        PermissionCardColumn(
                            properties = permissionCardProperties,
                            modifier = sharedModifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
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

private fun Modifier.animateDrawerProgressionBased(drawerState: DrawerState): Modifier = composed {
    val maxDrawerWidthPx =
        with(LocalDensity.current) { DrawerDefaults.MaximumDrawerWidth.toPx() }

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

    return@composed graphicsLayer(
        scaleX = drawerVisibilityPercentageInverse,
        scaleY = drawerVisibilityPercentageInverse,
        translationX = LocalConfiguration.current.screenWidthDp * drawerVisibilityPercentage,
        translationY = LocalConfiguration.current.screenHeightDp * drawerVisibilityPercentage,
        rotationY = drawerVisibilityPercentageAngle,
        rotationZ = drawerVisibilityPercentageAngle
    )
}

@Composable
fun PermissionCardColumn(
    properties: List<PermissionCardProperties>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.15f))
        AppFontText(
            text = stringResource(id = R.string.permissions_missing),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.fillMaxHeight(0.05f))
        InBetweenSpaced(
            elements = properties,
            makeElement = { PermissionCard(properties = it) },
            makeDivider = { Spacer(modifier = Modifier.fillMaxHeight(0.075f)) }
        )
    }
}

@Composable
internal fun MainContent(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    mainScreenVM: MainScreenViewModel = viewModel()
) {
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

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Spacer(modifier = Modifier.weight(0.075f))

        Box(modifier = Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
            FileTypeSelectionColumn(
                navigatorUIState = mainScreenVM.navigatorUIState,
                modifier = Modifier.fillMaxHeight()
            )
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