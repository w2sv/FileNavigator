package com.w2sv.filenavigator.ui.screen.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.PermissionState
import com.w2sv.androidutils.content.openAppSettings
import com.w2sv.common.util.goToManageExternalStorageSettings
import com.w2sv.composed.core.OnChange
import com.w2sv.composed.permissions.extensions.launchPermissionRequest
import com.w2sv.filenavigator.ui.AppViewModel
import com.w2sv.filenavigator.ui.LocalNavigator
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.sharedstate.AppPermissionsState
import com.w2sv.filenavigator.ui.sharedstate.RequiredPermission
import com.w2sv.filenavigator.ui.util.activityViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

@Composable
fun PermissionsScreenRoute(appVM: AppViewModel = activityViewModel(), navigator: Navigator = LocalNavigator.current) {
    val permissionsState = appVM.permissionsState
    val postNotificationsPermissionState = rememberPostNotificationsPermissionState()

    SyncPostNotificationsPermissionState(
        permissionState = postNotificationsPermissionState,
        appPermissionsState = permissionsState
    )

    val missingPermissions by permissionsState.missingPermissions.collectAsStateWithLifecycle()

    OnChange(missingPermissions) { if (it.isEmpty()) navigator.toHome() }

    val permissionCards = remember(missingPermissions) {
        buildPermissionCards(
            missingPermissions = missingPermissions.toPersistentList(),
            permissionsState = permissionsState,
            postNotificationsPermissionState = postNotificationsPermissionState
        )
    }

    PermissionsScreen(cards = permissionCards)
}

@Composable
private fun SyncPostNotificationsPermissionState(permissionState: PermissionState, appPermissionsState: AppPermissionsState) {
    OnChange(permissionState.status) { appPermissionsState.refresh() }
}

private fun buildPermissionCards(
    missingPermissions: ImmutableList<RequiredPermission>,
    permissionsState: AppPermissionsState,
    postNotificationsPermissionState: PermissionState
): ImmutableList<PermissionCard> =
    missingPermissions.map {
        when (it) {
            RequiredPermission.PostNotifications -> PermissionCard.postNotifications(
                onGrantButtonClick = { context ->
                    permissionsState.onPostNotificationsPermissionRequested()
                    postNotificationsPermissionState.launchPermissionRequest(
                        launchedBefore = permissionsState.postNotificationsPermissionRequested(),
                        onSuppressed = { context.openAppSettings() }
                    )
                }
            )

            RequiredPermission.ManageAllFiles -> PermissionCard.manageAllFiles(
                onGrantButtonClick = { context -> goToManageExternalStorageSettings(context) }
            )
        }
    }
        .toPersistentList()
