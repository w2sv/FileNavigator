package com.w2sv.filenavigator.ui.screens.missingpermissions

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.extensions.launchPermissionRequest

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    postNotificationsPermissionState: PermissionState?,
    modifier: Modifier = Modifier,
    appVM: AppViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    val manageAllFilesPermissionGranted by appVM.manageAllFilesPermissionGranted.collectAsStateWithLifecycle()

    val permissionCardProperties = remember(
        key1 = postNotificationsPermissionState?.status?.isGranted,
        key2 = manageAllFilesPermissionGranted
    ) {
        buildList {
            if (postNotificationsPermissionState?.status?.isGranted == false) {
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

    LazyColumn(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        items(permissionCardProperties, key = { it.hashCode() }) {
            PermissionCard(
                properties = it,
                modifier = Modifier.animateItemPlacement(
                    tween(
                        DefaultAnimationDuration
                    )
                )
            )
        }
    }
}