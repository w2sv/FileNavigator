package com.w2sv.navigator.system_action_broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import slimber.log.i

abstract class SystemActionBroadcastReceiver(private val action: String) : BroadcastReceiver() {

    private val logIdentifier: String
        get() = this::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        i { "$logIdentifier.onReceive" }

        if (intent == null || intent.action != action || context == null) return

        onReceiveMatchingIntent(context, intent)
    }

    protected abstract fun onReceiveMatchingIntent(context: Context, intent: Intent)

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