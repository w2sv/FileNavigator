package com.w2sv.filenavigator.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.w2sv.filenavigator.ui.components.drawer.NavigationDrawer
import com.w2sv.filenavigator.ui.components.drawer.animateBasedOnDrawerProgression
import com.w2sv.filenavigator.ui.screens.home.HomeScreen
import com.w2sv.filenavigator.ui.screens.missingpermissions.PermissionCardProperties
import com.w2sv.filenavigator.ui.screens.missingpermissions.PermissionScreen
import com.w2sv.filenavigator.ui.screens.navigatorsettings.NavigatorSettingsScreen
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.utils.extensions.launchPermissionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("NewApi")
@Composable
fun NavigationDrawerScreen(
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    appVM: AppViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val postNotificationsPermissionState =
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = {
                appVM.savePostNotificationsPermissionRequested()
            }
        )

    val manageAllFilesPermissionGranted by appVM.manageAllFilesPermissionGranted.collectAsState()

    val permissionCardProperties = remember(
        key1 = postNotificationsPermissionState.status.isGranted,
        key2 = manageAllFilesPermissionGranted
    ) {
        buildList {
            if (!postNotificationsPermissionState.status.isGranted) {
                add(
                    PermissionCardProperties(
                        iconRes = R.drawable.ic_notifications_24,
                        textRes = R.string.post_notifications_permission_rational,
                        onGrantButtonClick = {
                            postNotificationsPermissionState.launchPermissionRequest(
                                launchedBefore = appVM.postNotificationsPermissionRequested.value,
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
            if (!manageAllFilesPermissionGranted) {
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

    LaunchedEffect(permissionCardProperties) {
        appVM.setScreen(
            when (permissionCardProperties.size) {
                0 -> Screen.Home
                else -> Screen.MissingPermissions
            }
        )
    }

    NavigationDrawer(drawerState) {
        val screen by appVM.screen.collectAsState()

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
                        .animateBasedOnDrawerProgression(drawerState)
                        .padding(20.dp)

                AnimatedContent(targetState = screen, label = "") {
                    when (it) {
                        Screen.Home -> {
                            HomeScreen(
                                modifier = sharedModifier
                            )
                        }

                        Screen.MissingPermissions -> {
                            PermissionScreen(
                                properties = permissionCardProperties,
                                modifier = sharedModifier
                            )
                        }

                        Screen.NavigatorSettings -> {
                            NavigatorSettingsScreen(
                                returnToHomeScreen = { appVM.setScreen(Screen.Home) },
                                modifier = sharedModifier,
                                snackbarLaunchScope = scope
                            )
                        }
                    }
                }
            }
        }
    }

    BackHandler {
        when (drawerState.currentValue) {
            DrawerValue.Closed -> appVM.onBackPress(context)
            DrawerValue.Open -> scope.launch {
                drawerState.close()
            }
        }
    }
}