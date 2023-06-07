package com.w2sv.filenavigator.utils

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

@Suppress("DEPRECATION")
fun Context.sendLocalBroadcast(action: String) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(action))
}