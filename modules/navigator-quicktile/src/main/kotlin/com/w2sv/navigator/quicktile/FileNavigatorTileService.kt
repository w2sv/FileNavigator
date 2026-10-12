package com.w2sv.navigator.quicktile

import android.annotation.SuppressLint
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.IntDef
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.di.FileNavigatorIsRunning
import com.w2sv.navigator.domain.NavigatorIntents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FileNavigatorTileService : LoggingTileService() {

    @Inject
    internal lateinit var navigatorIntents: NavigatorIntents

    @Inject
    @FileNavigatorIsRunning
    internal lateinit var fileNavigatorIsRunning: StateFlow<Boolean>

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var listeningJob: Job? = null

    /**
     * Called every time the quick settings pan is expanded. onStopListening behaves vice-versa.
     */
    override fun onStartListening() {
        super.onStartListening()

        // Launch navigator and tile state synchronization job
        listeningJob?.cancel()
        listeningJob = scope.launch {
            fileNavigatorIsRunning.collect { isRunning ->
                qsTile.updateState(if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE)
            }
        }
    }

    override fun onClick() {
        super.onClick()

        when (qsTile.state) {
            Tile.STATE_ACTIVE -> FileNavigator.stop(this)
            Tile.STATE_INACTIVE -> activateNavigator()
        }
    }

    /**
     * Either
     * - starts the navigator immediately via launch dialog if permissions are granted
     * or
     * - launches MainActivity if required permissions are missing, which will result in the permissions screen being shown
     */
    private fun activateNavigator() {
        when (FileNavigator.areRequiredPermissionsGranted(this)) {
            true -> FileNavigator.start(this)
            false -> startMainActivityAndCollapse()
        }
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startMainActivityAndCollapse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(navigatorIntents.openMainActivityPending())
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(navigatorIntents.openMainActivity())
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        listeningJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

@IntDef(Tile.STATE_ACTIVE, Tile.STATE_INACTIVE, Tile.STATE_UNAVAILABLE)
@Retention(AnnotationRetention.SOURCE)
private annotation class TileState

private fun Tile.updateState(@TileState state: Int) {
    this.state = state
    updateTile()
}
