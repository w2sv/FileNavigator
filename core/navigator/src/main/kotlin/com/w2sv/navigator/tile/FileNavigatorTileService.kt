package com.w2sv.navigator.tile

import android.app.Dialog
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.core.navigator.R
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class FileNavigatorTileService : TileService() {

    @Inject
    @GlobalScope(AppDispatcher.Default)
    internal lateinit var scope: CoroutineScope

    @Inject
    internal lateinit var fileNavigatorStatusChanged: FileNavigator.StatusChanged

    override fun onStartListening() {
        super.onStartListening()

        i { "onStartListening" }
        scope.collectFromFlow(fileNavigatorStatusChanged.isRunning) { isRunning ->
            qsTile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            qsTile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        when (qsTile.state) {
            Tile.STATE_ACTIVE -> {
                FileNavigator.stop(this)
            }

            Tile.STATE_INACTIVE -> {
                showDialog(
                    Dialog(this)
                        .apply {
                            setContentView(R.layout.tile_dialog)
                            setOnShowListener {
                                FileNavigator.start(this@FileNavigatorTileService)
//                                dismiss()
                            }
                        }
                )
            }
        }
    }
}
