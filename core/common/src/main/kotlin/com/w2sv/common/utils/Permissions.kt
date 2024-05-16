package com.w2sv.common.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi

// ===========================
// External storage manager
// ===========================

@RequiresApi(Build.VERSION_CODES.R)
fun goToManageExternalStorageSettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.fromParts("package", context.packageName, null)
        )
    )
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
val manageExternalStoragePermissionRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

val isExternalStorageManger: Boolean
    get() = !manageExternalStoragePermissionRequired || Environment.isExternalStorageManager()

// ====================
// Post notifications
// ====================

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
val postNotificationsPermissionRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU