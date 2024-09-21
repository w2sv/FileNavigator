package com.w2sv.common.utils

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.w2sv.androidutils.graphics.loadBitmap
import slimber.log.i
import java.io.FileNotFoundException

fun ContentResolver.loadBitmapFileNotFoundHandled(uri: Uri): Bitmap? =
    try {
        loadBitmap(uri)
    } catch (e: FileNotFoundException) {
        i(e)
        null
    }

/**
 * Remedies "Failed query: java.lang.SecurityException: Permission Denial: opening provider com.android.externalstorage.ExternalStorageProvider from ProcessRecord{6fc17ee 8097:com.w2sv.filenavigator.debug/u0a753} (pid=8097, uid=10753) requires that you obtain access using ACTION_OPEN_DOCUMENT or related APIs"
 *
 * Tested with tree uri, dunno if working with others, too.
 */
fun ContentResolver.takePersistableReadAndWriteUriPermission(treeUri: Uri) {
    takePersistableUriPermission(
        treeUri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    )
}