package com.w2sv.navigator.system_action_broadcastreceiver

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.w2sv.androidutils.services.UnboundService
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class PowerSaveModeChangedReceiver :
    SystemActionBroadcastReceiver(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {

    @Inject
    internal lateinit var powerManager: PowerManager

    override fun onReceiveMatchingIntent(context: Context, intent: Intent) {
        if (powerManager.isPowerSaveMode) {
            FileNavigator.stop(context)
        }
    }

    class HostService : UnboundService() {

        private val powerSaveModeChangedReceiver by lazy {
            PowerSaveModeChangedReceiver()
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            i { "${this::class.java.simpleName}.onStartCommand | $intent" }

            try {
                powerSaveModeChangedReceiver.register(this)
            } catch (e: RuntimeException) {
                i(e)
            }

            return super.onStartCommand(intent, flags, startId)
        }

        override fun onDestroy() {
            super.onDestroy()

            i { "${this::class.java.simpleName}.onDestroy" }

            try {
                powerSaveModeChangedReceiver.unregister(this)
            } catch (e: RuntimeException) {
                i(e)
            }
        }

        companion object {
            fun getIntent(context: Context): Intent =
                Intent(context, HostService::class.java)
        }
    }
}