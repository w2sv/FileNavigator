package com.w2sv.filenavigator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.w2sv.filenavigator.service.FileNavigatorService
import slimber.log.i

class PowerSaveModeChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) return

        i { "Received intent $intent" }

        context?.run {
            getSystemService(PowerManager::class.java)?.isPowerSaveMode?.let { isPowerSaveMode ->
                if (isPowerSaveMode) {
                    FileNavigatorService.stop(applicationContext)
                } else {
                    FileNavigatorService.start(applicationContext)
                }
            }
        }
    }
}