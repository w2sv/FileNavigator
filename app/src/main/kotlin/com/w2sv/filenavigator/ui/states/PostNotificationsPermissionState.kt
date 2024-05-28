package com.w2sv.filenavigator.ui.states

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.common.utils.postNotificationsPermissionRequired
import com.w2sv.composed.OnChange

@Immutable
data class PostNotificationsPermissionState @OptIn(ExperimentalPermissionsApi::class) constructor(
    val state: PermissionState?
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberObservedPostNotificationsPermissionState(
    onPermissionResult: (Boolean) -> Unit,
    onStatusChanged: (Boolean) -> Unit
): PostNotificationsPermissionState =
    PostNotificationsPermissionState(
        state = if (postNotificationsPermissionRequired) {
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
    )
