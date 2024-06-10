package com.w2sv.navigator.system_action_broadcastreceiver

import android.content.Context
import android.content.Intent
import com.w2sv.navigator.FileNavigator
import slimber.log.i

class BootCompletedReceiver : SystemActionBroadcastReceiver(Intent.ACTION_BOOT_COMPLETED) {

    override fun onReceiveMatchingIntent(context: Context, intent: Intent) {
        i { "BootCompletedReceiver.onReceive" }

        FileNavigator.start(context)
    }
}