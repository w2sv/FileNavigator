@file:Suppress("DEPRECATION")

package com.w2sv.filenavigator.utils

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

fun Context.sendLocalBroadcast(action: String) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(action))
}

val Context.powerSaveModeActivated: Boolean?
    get() = getSystemService(PowerManager::class.java)?.isPowerSaveMode
