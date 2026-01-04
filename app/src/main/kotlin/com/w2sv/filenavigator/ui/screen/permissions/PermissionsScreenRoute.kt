package com.w2sv.filenavigator.ui.screen.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.w2sv.androidutils.openAppSettings
import com.w2sv.common.util.goToManageExternalStorageSettings
import com.w2sv.composed.core.CollectFromFlow
import com.w2sv.composed.core.OnChange
import com.w2sv.composed.permissions.extensions.launchPermissionRequest
import com.w2sv.filenavigator.ui.AppViewModel
import com.w2sv.filenavigator.ui.LocalNavigator
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.sharedstate.AppPermissionsState
import com.w2sv.filenavigator.ui.util.activityViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.filter

@Composable
fun PermissionsScreenRoute(appVM: AppViewModel = activityViewModel(), navigator: Navigator = LocalNavigator.current) {
    val permissionsState = appVM.permissionsState
    val postNotificationsPermissionState = rememberPostNotificationsPermissionState()

    SyncPostNotificationsPermissionState(
        permissionState = postNotificationsPermissionState,
        appPermissionsState = permissionsState
    )

    CollectFromFlow(permissionsState.allGranted.filter { true }) { allGranted ->
        if (allGranted) {
            navigator.toHome()
        }
    }

    val postNotificationsPermissionGranted by permissionsState.postNotificationsPermissionGranted.collectAsStateWithLifecycle()
    val manageAllFilesPermissionGranted by permissionsState.manageAllFilesPermissionGranted.collectAsStateWithLifecycle()

    val permissionCards = remember(
        postNotificationsPermissionGranted,
        manageAllFilesPermissionGranted
    ) {
        buildPermissionCards(
            postNotificationsPermissionGranted = postNotificationsPermissionGranted,
            manageAllFilesPermissionGranted = manageAllFilesPermissionGranted,
            permissionsState = permissionsState,
            postNotificationsPermissionState = postNotificationsPermissionState
        )
    }

    PermissionsScreen(cards = permissionCards)
}

@Composable
private fun SyncPostNotificationsPermissionState(permissionState: PermissionState, appPermissionsState: AppPermissionsState) {
    OnChange(permissionState.status) { status ->
        appPermissionsState.setPostNotificationsPermissionGranted(status.isGranted)
    }
}

private fun buildPermissionCards(
    postNotificationsPermissionGranted: Boolean,
    manageAllFilesPermissionGranted: Boolean,
    permissionsState: AppPermissionsState,
    postNotificationsPermissionState: PermissionState
): ImmutableList<PermissionCard> =
    buildList {
        if (!postNotificationsPermissionGranted) {
            add(
                PermissionCard.postNotifications(
                    onGrantButtonClick = { context ->
                        permissionsState.onPostNotificationsPermissionRequested()
                        postNotificationsPermissionState.launchPermissionRequest(
                            launchedBefore = permissionsState.postNotificationsPermissionRequested(),
                            onSuppressed = { context.openAppSettings() }
                        )
                    }
                )
            )
        }
        if (!manageAllFilesPermissionGranted) {
            add(PermissionCard.manageAllFiles(onGrantButtonClick = { context -> goToManageExternalStorageSettings(context) }))
        }
    }.toPersistentList()
