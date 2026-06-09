package com.w2sv.navigator.quicktile

import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.service.quicksettings.TileService
import com.w2sv.androidutils.content.toDebugString
import com.w2sv.core.logging.logIdentifier
import slimber.log.i

abstract class LoggingTileService : TileService() {

    init {
        i { "Lifecycle: $logIdentifier.init" }
    }

    override fun onCreate() {
        i { "Lifecycle: $logIdentifier.onCreate" }
        super.onCreate()
    }

    override fun onTileAdded() {
        i { "Lifecycle: $logIdentifier.onTileAdded" }
        super.onTileAdded()
    }

    override fun onTileRemoved() {
        i { "Lifecycle: $logIdentifier.onTileRemoved" }
        super.onTileRemoved()
    }

    override fun onStartListening() {
        i { "Lifecycle: $logIdentifier.onStartListening" }
        super.onStartListening()
    }

    override fun onStopListening() {
        i { "Lifecycle: $logIdentifier.onStopListening" }
        super.onStopListening()
    }

    override fun onClick() {
        i { "Lifecycle: $logIdentifier.onClick" }
        super.onClick()
    }

    override fun onBind(intent: Intent?): IBinder? {
        i { "Lifecycle: $logIdentifier.onBind | intent=${intent?.toDebugString()}" }
        return super.onBind(intent)
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

    override fun onDestroy() {
        i { "Lifecycle: $logIdentifier.onDestroy" }
        super.onDestroy()
    }
}
