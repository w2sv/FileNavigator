package com.w2sv.filenavigator.ui.screen.navigatorsettings.list

import android.app.StatusBarManager
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.w2sv.androidutils.content.componentName
import com.w2sv.androidutils.service.systemService
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.util.snackbar.ScopedSnackbarController
import com.w2sv.filenavigator.ui.util.snackbar.rememberScopedSnackbarController
import com.w2sv.modules.common.R
import com.w2sv.navigator.quicktile.FileNavigatorTileService
import slimber.log.d
import slimber.log.e

// TODO: debounce
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun rememberRequestAddFileNavigatorTile(
    context: Context = LocalContext.current,
    snackbarController: ScopedSnackbarController = rememberScopedSnackbarController()
): () -> Unit =
    remember(context, snackbarController) {
        {
            val statusBarManager = context.systemService<StatusBarManager>()
            requestAddFileNavigatorTile(context, statusBarManager) { result ->
                when (result) {
                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> snackbarController.showReplacing {
                        AppSnackbarVisuals(
                            getString(R.string.tile_successully_added),
                            kind = SnackbarKind.Success
                        )
                    }

                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> snackbarController.showReplacing {
                        AppSnackbarVisuals(
                            getString(R.string.tile_already_added),
                            kind = SnackbarKind.Info
                        )
                    }

                    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> snackbarController.showReplacing {
                        AppSnackbarVisuals(
                            getString(R.string.tile_adding_unsuccessful),
                            kind = SnackbarKind.Error
                        )
                    }

                    StatusBarManager.TILE_ADD_REQUEST_ERROR_REQUEST_IN_PROGRESS -> e { "TILE_ADD_REQUEST_ERROR_REQUEST_IN_PROGRESS" }

                    else -> d { "Received result $result" }
                }
            }
        }
    }

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun requestAddFileNavigatorTile(context: Context, statusBarManager: StatusBarManager, onResult: (Int) -> Unit) {
    statusBarManager.requestAddTileService(
        componentName<FileNavigatorTileService>(context),
        context.getString(R.string.app_name),
        Icon.createWithResource(context, R.drawable.ic_app_logo_24),
        context.mainExecutor,
        onResult
    )
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
val isRequestQuickSettingsTileSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
