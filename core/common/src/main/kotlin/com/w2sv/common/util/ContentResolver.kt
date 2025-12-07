package com.w2sv.common.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.w2sv.androidutils.graphics.loadBitmap
import com.w2sv.androidutils.hasPermission
import java.io.FileNotFoundException
import slimber.log.e

fun ContentResolver.loadBitmapWithFileNotFoundHandling(uri: Uri): Bitmap? =
    try {
        loadBitmap(uri)
    } catch (e: FileNotFoundException) {
        e(e)
        null
    }

/**
 * Remedies "Failed query: java.lang.SecurityException: Permission Denial: opening provider com.android.externalstorage.ExternalStorageProvider from ProcessRecord{6fc17ee 8097:com.w2sv.filenavigator.debug/u0a753} (pid=8097, uid=10753) requires that you obtain access using ACTION_OPEN_DOCUMENT or related APIs"
 */
fun ContentResolver.takePersistableReadAndWriteUriPermission(treeUri: Uri) {
    takePersistableUriPermission(
        treeUri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    )
}

fun Uri.hasReadAndWritePermission(context: Context): Boolean =
    hasPermission(context, Intent.FLAG_GRANT_READ_URI_PERMISSION) &&
        hasPermission(
            context,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
