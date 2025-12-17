package com.w2sv.navigator.system_broadcastreceiver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.w2sv.common.util.LoggingBroadcastReceiver
import com.w2sv.common.util.logIdentifier
import slimber.log.i

abstract class DynamicBroadcastReceiver(private val action: String) : LoggingBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == action) {
            onReceiveMatchingIntent(context, intent)
        }
    }

    abstract fun onReceiveMatchingIntent(context: Context, intent: Intent)

    fun setRegistered(register: Boolean, context: Context) {
        try {
            if (register) {
                register(context)
            } else {
                unregister(context)
            }
        } catch (_: IllegalArgumentException) {
            // Thrown when attempting to unregister an already unregistered receiver
        }
    }

    fun register(context: Context) {
        context.registerReceiver(
            this,
            IntentFilter(action)
        )
        i { "Registered $logIdentifier" }
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
        i { "Unregistered $logIdentifier" }
    }
}
