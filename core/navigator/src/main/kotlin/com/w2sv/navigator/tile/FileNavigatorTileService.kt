package com.w2sv.navigator.tile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.IntDef
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.core.navigator.R
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
    internal lateinit var fileNavigatorStatusChanged: FileNavigator.StatusChanged

    override fun onStartListening() {
        super.onStartListening()

        i { "onStartListening" }

        if (!isServiceRunning<FileNavigator>()) {
            updateTileState(Tile.STATE_INACTIVE)
        }

        scope.collectFromFlow(fileNavigatorStatusChanged.isRunning) { isRunning ->
            updateTileState(if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE)
        }
    }

    override fun onClick() {
        super.onClick()

        when (qsTile.state) {
            Tile.STATE_ACTIVE -> {
                FileNavigator.stop(this)
            }

            Tile.STATE_INACTIVE -> {
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

    private fun showDialogAndLaunchNavigator() {
        showDialog(
            Dialog(this)
                .apply {
                    setTheme(com.afollestad.materialdialogs.R.style.MD_Dark)
                    setContentView(R.layout.tile_dialog)
                    setOnShowListener {
                        scope.launchDelayed(250) {
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
