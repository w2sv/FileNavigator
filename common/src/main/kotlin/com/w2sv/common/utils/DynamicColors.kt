package com.w2sv.common.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
val dynamicColorsSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S