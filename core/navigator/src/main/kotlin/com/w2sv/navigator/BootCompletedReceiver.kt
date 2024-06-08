package com.w2sv.navigator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import slimber.log.i

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        i { "BootCompletedReceiver.onReceive" }

        if (intent == null || intent.action != Intent.ACTION_BOOT_COMPLETED || context == null) return

        FileNavigator.start(context)
    }

    fun register(context: Context) {
        context.registerReceiver(
            this,
            IntentFilter()
                .apply {
                    addAction(Intent.ACTION_BOOT_COMPLETED)
                }
        )
        i { "Registered ${this::class.java.simpleName}" }
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
        i { "Unregistered ${this::class.java.simpleName}" }
    }
}