package com.w2sv.navigator.quicktile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.IntDef
import com.w2sv.androidutils.hasPermission
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.core.navigator.R
import com.w2sv.kotlinutils.coroutines.collectFromFlow
import com.w2sv.kotlinutils.coroutines.launchDelayed
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.shared.mainActivityIntent
import com.w2sv.navigator.shared.mainActivityPendingIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class FileNavigatorTileService : TileService() {

    @Inject
    @GlobalScope(AppDispatcher.Default)
    internal lateinit var scope: CoroutineScope

    @Inject
    internal lateinit var fileNavigatorIsRunning: FileNavigator.IsRunning

    /**
     * Called every time the quick tile pan is expanded. onStopListening behaves vice-versa.
     */
    override fun onStartListening() {
        i { "onStartListening" }

        // Update tile state reactively on navigator status change
        scope.collectFromFlow(fileNavigatorIsRunning) { isRunning ->
            updateTileState(if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE)
        }
    }

    override fun onClick() {
        when (qsTile.state) {
            Tile.STATE_ACTIVE -> {
                FileNavigator.stop(this)
            }

            Tile.STATE_INACTIVE -> {
                // Start navigator if all required permissions are granted, otherwise launch MainActivity, which will invoke the 'Required Permissions' screen
                if (isExternalStorageManger && hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                    showDialogAndLaunchNavigator()
                } else {
                    startMainActivityAndCollapse()
                }
            }
        }
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startMainActivityAndCollapse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(mainActivityPendingIntent(this))
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(mainActivityIntent(this))
        }
    }

    /**
     * Starting from Sdk Version 31 (Android 12), foreground services can't be started from the background.
     * Therefore show a Dialog, which promotes the app to a foreground process state, and start the FGS while it's showing.
     *
     * https://developer.android.com/develop/background-work/services/foreground-services#bg-access-restrictions
     * https://stackoverflow.com/questions/77331327/start-a-foreground-service-from-a-quick-tile-on-android-targetsdkversion-34-and
     */
    private fun showDialogAndLaunchNavigator() {
        showDialog(
            Dialog(this)
                .apply {
                    setTheme(R.style.RoundedCornersAlertDialog)
                    setContentView(R.layout.tile_dialog)
                    setOnShowListener {
                        scope.launchDelayed(250) {  // Add small delay to make the dialog visible for a bit longer than merely a couple of milliseconds for better UX
                            FileNavigator.start(this@FileNavigatorTileService)
                            dismiss()
                        }
                    }
                }
        )
    }
}

@IntDef(Tile.STATE_ACTIVE, Tile.STATE_INACTIVE, Tile.STATE_UNAVAILABLE)
private annotation class TileState

private fun TileService.updateTileState(@TileState state: Int) {
    qsTile.state = state
    qsTile.updateTile()
}
