package com.w2sv.filenavigator.ui.screens.missingpermissions

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.composed.permissions.extensions.launchPermissionRequest
import com.w2sv.filenavigator.PostNotificationsPermissionState
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.utils.ModifierReceivingComposable
import com.w2sv.filenavigator.ui.utils.activityViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Destination<RootGraph>
@Composable
fun MissingPermissionsScreen(
    postNotificationsPermissionState: PostNotificationsPermissionState,
    modifier: Modifier = Modifier,
) {
    val permissionCards =
        rememberMovablePermissionCards(postNotificationsPermissionState = postNotificationsPermissionState.state)

    if (isPortraitModeActive) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            permissionCards.forEach {
                it(Modifier)
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            permissionCards.forEach {
                it(
                    Modifier
                        .fillMaxWidth(0.4f)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberMovablePermissionCards(
    postNotificationsPermissionState: PermissionState?,
    appVM: AppViewModel = activityViewModel(),
    context: Context = LocalContext.current
): List<ModifierReceivingComposable> {
    val manageAllFilesPermissionGranted by appVM.manageAllFilesPermissionGranted.collectAsStateWithLifecycle()

    return remember(
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
                                onSuppressed = {
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
            .map { properties ->
                movableContentOf { mod -> PermissionCard(properties = properties, modifier = mod) }
            }
    }
}