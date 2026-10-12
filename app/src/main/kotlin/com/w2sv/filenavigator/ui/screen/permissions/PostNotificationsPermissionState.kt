package com.w2sv.filenavigator.ui.screen.permissions

import android.Manifest
import androidx.compose.runtime.Composable
import com.w2sv.androidutils.os.postNotificationsPermissionRequired
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.augmentedpermissions.PermissionState
import com.w2sv.augmentedpermissions.rememberPermissionState
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun rememberPostNotificationsPermissionState(requestHistory: PermissionRequestHistory): PermissionState =
    if (postNotificationsPermissionRequired) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS, requestHistory = requestHistory)
    } else {
        grantedPermissionState
    }

private val grantedPermissionState by threadUnsafeLazy {
    object : PermissionState {
        override val isGranted = true
        override val shouldShowRationale = false
        override val isLaunchingSuppressed = false
        override val revokedPermissions = emptyList<String>()
        override val grantedFromRequest = MutableSharedFlow<Boolean>()
        override fun launchRequest(onSuppressed: (() -> Unit)?) {}
    }
}
