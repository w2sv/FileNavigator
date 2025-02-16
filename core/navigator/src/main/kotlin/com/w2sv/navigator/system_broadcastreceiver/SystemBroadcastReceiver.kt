package com.w2sv.navigator.system_broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import slimber.log.i

abstract class SystemBroadcastReceiver(private val action: String) : BroadcastReceiver() {

    private val logIdentifier: String
        get() = this::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        i { "$logIdentifier.onReceive" }

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
