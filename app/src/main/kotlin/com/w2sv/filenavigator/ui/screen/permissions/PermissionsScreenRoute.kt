package com.w2sv.filenavigator.ui.screen.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.androidutils.content.openAppSettings
import com.w2sv.augmentedpermissions.PermissionState
import com.w2sv.composed.runtime.OnChange
import com.w2sv.core.util.goToManageExternalStorageSettings
import com.w2sv.filenavigator.ui.shared.permissions.AppPermissionsState
import com.w2sv.filenavigator.ui.shared.permissions.RequiredPermission
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

@Composable
fun PermissionsScreenRoute(permissionsState: AppPermissionsState, onAllPermissionsGranted: () -> Unit) {
    val postNotificationsPermissionState = rememberPostNotificationsPermissionState(permissionsState.postNotificationsPermissionHistory)

    SyncPostNotificationsPermissionState(
        permissionState = postNotificationsPermissionState,
        appPermissionsState = permissionsState
    )

    val missingPermissions by permissionsState.missingPermissions.collectAsStateWithLifecycle()

    OnChange(missingPermissions) { if (it.isEmpty()) onAllPermissionsGranted() }

    val permissionCards = remember(missingPermissions) {
        buildPermissionCards(
            missingPermissions = missingPermissions.toPersistentList(),
            postNotificationsPermissionState = postNotificationsPermissionState
        )
    }

    PermissionsScreen(cards = permissionCards)
}

@Composable
private fun SyncPostNotificationsPermissionState(permissionState: PermissionState, appPermissionsState: AppPermissionsState) {
    OnChange(permissionState.isGranted) { appPermissionsState.refresh() }
}

private fun buildPermissionCards(
    missingPermissions: ImmutableList<RequiredPermission>,
    postNotificationsPermissionState: PermissionState
): ImmutableList<PermissionCard> =
    missingPermissions.map {
        when (it) {
            RequiredPermission.PostNotifications -> PermissionCard.postNotifications(
                onGrantButtonClick = { context ->
                    postNotificationsPermissionState.launchRequest(onSuppressed = { context.openAppSettings() })
                }
            )

            RequiredPermission.ManageAllFiles -> PermissionCard.manageAllFiles(
                onGrantButtonClick = { context -> goToManageExternalStorageSettings(context) }
            )
        }
    }
        .toPersistentList()
