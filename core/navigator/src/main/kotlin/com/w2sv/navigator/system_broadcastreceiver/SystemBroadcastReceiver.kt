package com.w2sv.navigator.system_broadcastreceiver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.w2sv.common.util.LoggingBroadcastReceiver
import com.w2sv.common.util.logIdentifier
import slimber.log.i

abstract class SystemBroadcastReceiver(private val action: String) : LoggingBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action != action) return

        onReceiveMatchingIntent(context, intent)
    }

    protected abstract fun onReceiveMatchingIntent(context: Context, intent: Intent)

    fun toggle(register: Boolean, context: Context) {
        try {
            if (register) {
                register(context)
            } else {
                unregister(context)
            }
        } catch (_: IllegalArgumentException) { // Thrown upon attempting to unregister unregistered receiver
        }
    }

    fun register(context: Context) {
        context.registerReceiver(
            this,
            IntentFilter()
                .apply {
                    addAction(action)
                }
        )
        i { "Registered $logIdentifier" }
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
        i { "Unregistered $logIdentifier" }
    }
}
