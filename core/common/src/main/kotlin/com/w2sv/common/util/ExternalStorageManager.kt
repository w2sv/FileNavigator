package com.w2sv.common.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.w2sv.androidutils.os.manageExternalStoragePermissionRequired

@RequiresApi(Build.VERSION_CODES.R)
fun goToManageExternalStorageSettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.fromParts("package", context.packageName, null)
        )
    )
}

val isExternalStorageManger: Boolean
    get() = !manageExternalStoragePermissionRequired || Environment.isExternalStorageManager()
