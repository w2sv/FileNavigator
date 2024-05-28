package com.w2sv.common.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import slimber.log.i
import java.io.FileNotFoundException

fun ContentResolver.loadBitmap(uri: Uri): Bitmap? =
    try {
        BitmapFactory.decodeStream(openInputStream(uri))
    } catch (e: FileNotFoundException) {
        i(e)
        null
    }