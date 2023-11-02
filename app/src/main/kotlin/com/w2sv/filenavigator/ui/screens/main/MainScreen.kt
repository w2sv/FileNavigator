package com.w2sv.filenavigator.ui.screens.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppSnackbar
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.AppTopBar
import com.w2sv.filenavigator.ui.components.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.components.PermissionCardProperties
import com.w2sv.filenavigator.ui.components.drawer.NavigationDrawer
import com.w2sv.filenavigator.ui.components.drawer.animateBasedOnDrawerProgression
import com.w2sv.filenavigator.ui.screens.AppViewModel
import com.w2sv.filenavigator.ui.screens.main.components.PermissionScreen
import com.w2sv.filenavigator.ui.screens.main.components.statusdisplay.StatusDisplay
import com.w2sv.filenavigator.ui.states.NavigatorState
import com.w2sv.filenavigator.ui.utils.extensions.closeAnimated
import com.w2sv.filenavigator.ui.utils.extensions.launchPermissionRequest
import com.w2sv.filenavigator.ui.utils.extensions.openAnimated
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("NewApi")
@Composable
fun UI(
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    mainScreenVM: MainScreenViewModel = viewModel(),
    appVM: AppViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val postNotificationsPermissionState =
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = {
                mainScreenVM.savePostNotificationsPermissionRequested()
            }
        )

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
                        iconRes = R.drawable.ic_folder_open_24,
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
                        .animateBasedOnDrawerProgression(drawerState)

                AnimatedContent(targetState = permissionCardProperties.isEmpty(), label = "") {
                    if (it) {
                        MainScreen(
                            navigatorState = mainScreenVM.navigatorState,
                            modifier = sharedModifier.padding(horizontal = 32.dp)
                        )
                    } else {
                        PermissionScreen(
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

@Composable
private fun MainScreen(
    navigatorState: NavigatorState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        StatusDisplay(
            navigatorState = navigatorState,
            modifier = Modifier
                .fillMaxHeight(0.3f)
                .fillMaxWidth()
        )
    }
}