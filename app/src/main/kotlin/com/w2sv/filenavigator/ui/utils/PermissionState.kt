package com.w2sv.filenavigator.ui.utils

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
fun PermissionState.launchPermissionRequest(launchedBefore: Boolean, onBlocked: () -> Unit) {
    if (!launchedBefore || status.shouldShowRationale)
        launchPermissionRequest()
    else
        onBlocked()
}