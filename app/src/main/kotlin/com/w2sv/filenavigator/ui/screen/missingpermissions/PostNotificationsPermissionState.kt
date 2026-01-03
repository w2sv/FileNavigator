package com.w2sv.filenavigator.ui.screen.missingpermissions

import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.os.postNotificationsPermissionRequired
import com.w2sv.kotlinutils.threadUnsafeLazy

@Composable
fun rememberPostNotificationsPermissionState(): PermissionState =
    if (postNotificationsPermissionRequired) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        grantedPermissionState
    }

private val grantedPermissionState by threadUnsafeLazy {
    object : PermissionState {
        override val permission: String = ""
        override val status: PermissionStatus = PermissionStatus.Granted
        override fun launchPermissionRequest() {}
    }
}
