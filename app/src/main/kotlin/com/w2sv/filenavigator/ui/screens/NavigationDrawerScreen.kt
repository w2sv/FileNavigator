package com.w2sv.filenavigator.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.common.utils.postNotificationsPermissionRequired
import com.w2sv.composed.OnChange
import com.w2sv.filenavigator.ui.designsystem.AppSnackbar
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.AppTopBar
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.drawer.NavigationDrawer
import com.w2sv.filenavigator.ui.designsystem.drawer.drawerRepelledAnimation
import com.w2sv.filenavigator.ui.screens.home.HomeScreen
import com.w2sv.filenavigator.ui.screens.missingpermissions.PermissionScreen
import com.w2sv.filenavigator.ui.screens.navigatorsettings.NavigatorSettingsScreen
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NavigationDrawerScreen(
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    appVM: AppViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val postNotificationsPermissionState =
        rememberObservedPostNotificationsPermissionState(
            onPermissionResult = { appVM.savePostNotificationsPermissionRequestedIfRequired() },
            onStatusChanged = appVM::setPostNotificationsPermissionGranted
        )

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    NavigationDrawer(state = drawerState) {
        val screen by appVM.screen.collectAsStateWithLifecycle()

        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { snackbarData ->
                    AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
                }
            },
            topBar = {
                AppTopBar(
                    title = stringResource(id = screen.titleRes),
                    onNavigationIconClick = {
                        scope.launch {
                            drawerState.open()
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
                        .drawerRepelledAnimation(drawerState)
                        .padding(20.dp)

                AnimatedContent(targetState = screen, label = "ScreenChangeAnimation") {
                    when (it) {
                        Screen.Home -> {
                            HomeScreen(
                                modifier = sharedModifier
                            )
                        }

                        Screen.MissingPermissions -> {
                            PermissionScreen(
                                postNotificationsPermissionState = postNotificationsPermissionState,
                                modifier = sharedModifier
                            )
                        }

                        Screen.NavigatorSettings -> {
                            NavigatorSettingsScreen(
                                returnToHomeScreen = { appVM.setScreen(Screen.Home) },
                                parentScope = scope,
                                modifier = sharedModifier
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberObservedPostNotificationsPermissionState(
    onPermissionResult: (Boolean) -> Unit,
    onStatusChanged: (Boolean) -> Unit
): PermissionState? =
    if (postNotificationsPermissionRequired()) {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = onPermissionResult
        )
            .also {
                OnChange(value = it.status) { status ->
                    onStatusChanged(status.isGranted)
                }
            }
    } else {
        null
    }
