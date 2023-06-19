package com.w2sv.filenavigator.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.w2sv.androidutils.permissions.hasPermission

@RequiresApi(Build.VERSION_CODES.R)
fun goToManageExternalStorageSettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.fromParts("package", context.packageName, null)
        )
    )
}

fun manageExternalStoragePermissionRequired(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

fun isExternalStorageManger(): Boolean =
    !manageExternalStoragePermissionRequired() || Environment.isExternalStorageManager()

enum class StorageAccessStatus {
    NoAccess,
    MediaFilesOnly,
    AllFiles;

    companion object {
        fun get(context: Context): StorageAccessStatus =
            when{
                isExternalStorageManger() -> AllFiles
                context.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> MediaFilesOnly
                else -> NoAccess
            }
    }
}