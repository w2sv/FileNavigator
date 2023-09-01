package com.w2sv.data.model

import android.Manifest
import android.content.Context
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.common.utils.isExternalStorageManger

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