package com.w2sv.filenavigator

import android.Manifest
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.benchmark.macro.MacrobenchmarkScope

/**
 * [Source](https://github.com/android/nowinandroid/blob/main/benchmarks/src/main/kotlin/com/google/samples/apps/nowinandroid/GeneralActions.kt)
 */
fun MacrobenchmarkScope.allowNotifications() {
    if (SDK_INT >= TIRAMISU) {
        val command = "pm grant $packageName ${Manifest.permission.POST_NOTIFICATIONS}"
        device.executeShellCommand(command)
    }
}
