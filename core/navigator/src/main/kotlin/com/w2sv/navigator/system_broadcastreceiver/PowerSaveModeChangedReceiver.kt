package com.w2sv.navigator.system_broadcastreceiver

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import slimber.log.i

@AndroidEntryPoint
class PowerSaveModeChangedReceiver : DynamicBroadcastReceiver(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {

    @Inject
    internal lateinit var powerManager: PowerManager

    override fun onReceiveMatchingIntent(context: Context, intent: Intent) {
        if (powerManager.isPowerSaveMode) {
            FileNavigator.stop(context)
            i { "Stopped navigator due to power save mode" }
        }
    }
}
