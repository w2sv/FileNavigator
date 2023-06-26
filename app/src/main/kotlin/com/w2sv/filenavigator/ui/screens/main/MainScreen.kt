package com.w2sv.filenavigator.ui.screens.main

import android.annotation.SuppressLint
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
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
import com.w2sv.filenavigator.datastore.PreferencesKey
import com.w2sv.filenavigator.ui.components.AppSnackbar
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.utils.goToManageExternalStorageSettings
import com.w2sv.filenavigator.utils.toEasing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(mainScreenViewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val springSpec = FloatSpringSpec(Spring.DampingRatioMediumBouncy)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val openDrawer: () -> Unit = {
        scope.launch {
            drawerState.animateTo(DrawerValue.Open, springSpec)
        }
    }
    val closeDrawer: () -> Unit = {
        scope.launch {
            drawerState.animateTo(
                DrawerValue.Closed,
                springSpec
            )
        }
    }

    NavigationDrawer(drawerState, closeDrawer) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(mainScreenViewModel.snackbarHostState) { snackbarData ->
                    AppSnackbar(snackbarData = snackbarData)
                }
            },
            topBar = {
                AppTopBar(onNavigationIconClick = openDrawer)
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

@OptIn(ExperimentalAnimationApi::class)
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
                        slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = DefaultAnimationDuration,
                                easing = AnticipateOvershootInterpolator().toEasing()
                            ),
                            initialOffsetX = { -it }
                        ) + scaleIn(
                            animationSpec = tween(
                                durationMillis = DefaultAnimationDuration
                            )
                        ) with slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(
                                durationMillis = DefaultAnimationDuration,
                                easing = AnticipateOvershootInterpolator().toEasing()
                            )
                        ) + scaleOut(
                            animationSpec = tween(
                                durationMillis = DefaultAnimationDuration
                            )
                        )
                    },
                    label = ""
                ) {
                    if (it)
                        NavigatorConfigurationButtons()
                    else
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

@SuppressLint("NewApi")
@Composable
internal fun EventualManageExternalStoragePermissionRational(mainScreenViewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showManageExternalStorageRational by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                val onDismissRequest: () -> Unit = {
                    coroutineScope.launch {
                        mainScreenViewModel.saveToDataStore(
                            PreferencesKey.SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL,
                            true
                        )
                        value = false
                    }
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

    if (!mainScreenViewModel.anyStorageAccessGranted.collectAsState().value && !mainScreenViewModel.dataStoreRepository.showedManageExternalStorageRational.collectAsState(
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