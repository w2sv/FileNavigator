package com.w2sv.filenavigator.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.localbroadcastmanager.content.LocalBroadcastManager

@Suppress("DEPRECATION")
fun Context.sendLocalBroadcast(action: String) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(action))
}

val Context.powerSaveModeActivated: Boolean?
    get() = getSystemService(PowerManager::class.java)?.isPowerSaveMode

fun goToAppSettings(context: Context) {
    context.startActivity(
        Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(
                Uri.fromParts(
                    "package",
                    context.packageName,
                    null
                )
            )
    )
}