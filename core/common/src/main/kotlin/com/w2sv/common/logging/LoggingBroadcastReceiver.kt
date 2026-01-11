package com.w2sv.common.logging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper
import com.w2sv.androidutils.content.logString
import slimber.log.i

/**
 * A [BroadcastReceiver] that logs upon its [onReceive] being called.
 */
abstract class LoggingBroadcastReceiver : BroadcastReceiver() {

    @CallSuper
    override fun onReceive(context: Context, intent: Intent) {
        i { "$logIdentifier.onReceive | ${intent.logString()}" }
    }
}
