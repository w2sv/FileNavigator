package com.w2sv.navigator.system_broadcastreceiver

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import slimber.log.i

@AndroidEntryPoint
internal class PowerSaveModeChangedReceiver :
    SystemBroadcastReceiver(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {

    @Inject
    internal lateinit var powerManager: PowerManager

    override fun onReceiveMatchingIntent(context: Context, intent: Intent) {
        if (powerManager.isPowerSaveMode) {
            i { "Stopping FileNavigator due to power save mode" }
            FileNavigator.stop(context)
        }
    }
}
