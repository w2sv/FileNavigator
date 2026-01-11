package com.w2sv.common.logging

import android.content.Intent
import android.content.res.Configuration
import com.w2sv.androidutils.content.logString
import com.w2sv.androidutils.service.UnboundService
import slimber.log.i

abstract class LifecycleLoggingUnboundService : UnboundService() {

    init {
        i { "Lifecycle: $logIdentifier.init" }
    }

    override fun onCreate() {
        i { "Lifecycle: $logIdentifier.onCreate" }
        super.onCreate()
    }

    protected fun logOnStartCommand(intent: Intent?) {
        i { "Lifecycle: $logIdentifier.onStartCommand | intent=${intent?.logString()}" }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logOnStartCommand(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        i { "Lifecycle: $logIdentifier.onConfigurationChanged | newConfig=$newConfig" }
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        i { "Lifecycle: $logIdentifier.onLowMemory" }
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        i { "Lifecycle: $logIdentifier.onTrimMemory | level=$level" }
        super.onTrimMemory(level)
    }

    override fun onTimeout(startId: Int) {
        i { "Lifecycle: $logIdentifier.onTimeout" }
        super.onTimeout(startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        i { "Lifecycle: $logIdentifier.onTaskRemoved | rootIntent=${rootIntent?.logString()}" }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        i { "Lifecycle: $logIdentifier.onDestroy" }
        super.onDestroy()
    }
}
