package com.w2sv.filenavigator.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import com.w2sv.androidutils.services.UnboundService
import com.w2sv.filenavigator.navigator.service.FileNavigatorService
import com.w2sv.filenavigator.utils.powerSaveModeActivated
import slimber.log.i

class PowerSaveModeChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) return

        i { "Received intent $intent" }

        context?.run {
            when (powerSaveModeActivated) {
                true -> FileNavigatorService.stop(applicationContext)
                false -> FileNavigatorService.start(applicationContext)
                else -> Unit
            }
        }
    }

    fun register(context: Context) {
        context.registerReceiver(
            this,
            IntentFilter()
                .apply {
                    addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
                }
        )
        i { "Registered ${this::class.java.simpleName}" }
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
        i { "Unregistered ${this::class.java.simpleName}" }
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