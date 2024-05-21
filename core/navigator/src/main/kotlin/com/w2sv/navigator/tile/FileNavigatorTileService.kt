package com.w2sv.navigator.tile

import android.app.Dialog
import android.service.quicksettings.TileService
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.shared.launchMainActivityPendingIntent
import com.w2sv.navigator.shared.mainActivityIntent

class FileNavigatorTileService: TileService() {

    // Called when the user adds your tile.
    override fun onTileAdded() {
        super.onTileAdded()
    }
    // Called when your app can update your tile.
    override fun onStartListening() {
        super.onStartListening()
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        super.onStopListening()
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()

        startActivityAndCollapse(mainActivityIntent(this))
    }
    // Called when the user removes your tile.
    override fun onTileRemoved() {
        super.onTileRemoved()
    }
}
