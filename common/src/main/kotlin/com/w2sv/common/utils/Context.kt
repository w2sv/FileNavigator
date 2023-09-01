package com.w2sv.common.utils

import android.content.Context
import android.os.PowerManager

val Context.powerSaveModeActivated: Boolean?
    get() = getSystemService(PowerManager::class.java)?.isPowerSaveMode
