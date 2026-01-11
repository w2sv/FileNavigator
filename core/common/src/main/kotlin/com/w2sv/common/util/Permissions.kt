package com.w2sv.common.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import com.w2sv.androidutils.content.hasPermission
import com.w2sv.androidutils.os.manageExternalStoragePermissionRequired
import com.w2sv.androidutils.os.postNotificationsPermissionRequired

fun goToManageExternalStorageSettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.fromParts("package", context.packageName, null)
        )
    )
}

/**
 * @return true for API < 30 where [Manifest.permission.MANAGE_EXTERNAL_STORAGE] didn't yet exist, otherwise checks whether the permission
 * has been granted.
 */
val hasManageAllFilesPermission: Boolean
    get() = !manageExternalStoragePermissionRequired || Environment.isExternalStorageManager()

/**
 * @return true for API < 33 where [Manifest.permission.POST_NOTIFICATIONS] didn't yet exist, otherwise checks whether the permission has
 * been granted.
 */
fun Context.hasPostNotificationsPermission(): Boolean =
    !postNotificationsPermissionRequired || hasPermission(Manifest.permission.POST_NOTIFICATIONS)
