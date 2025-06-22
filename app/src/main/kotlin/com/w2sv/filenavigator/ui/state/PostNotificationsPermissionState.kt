package com.w2sv.filenavigator.ui.state

import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.os.postNotificationsPermissionRequired
import com.w2sv.composed.OnChange

@Composable
fun rememberPostNotificationsPermissionState(onPermissionResult: (Boolean) -> Unit, onStatusChanged: (Boolean) -> Unit): PermissionState =
    if (postNotificationsPermissionRequired) {
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
        GrantedPermissionState
    }

private object GrantedPermissionState : PermissionState {
    override val permission: String = ""
    override val status: PermissionStatus = PermissionStatus.Granted
    override fun launchPermissionRequest() {}
}
