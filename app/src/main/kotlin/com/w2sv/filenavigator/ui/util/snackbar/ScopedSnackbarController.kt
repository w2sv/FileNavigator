package com.w2sv.filenavigator.ui.util.snackbar

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Non-suspending façade over [SnackbarController] that launches snackbar actions
 * in a provided [CoroutineScope] for fire-and-forget usage from event handlers.
 */
@Stable
class ScopedSnackbarController(private val presenter: SnackbarController, private val scope: CoroutineScope) {
    fun show(makeSnackbar: Context.() -> SnackbarVisuals) {
        scope.launch { presenter.show(makeSnackbar = makeSnackbar) }
    }

    fun showReplacing(makeSnackbar: Context.() -> SnackbarVisuals) {
        scope.launch { presenter.showReplacing(makeSnackbar = makeSnackbar) }
    }
}

@Composable
fun rememberScopedSnackbarController(
    presenter: SnackbarController = rememberSnackbarController(),
    scope: CoroutineScope = rememberCoroutineScope()
): ScopedSnackbarController =
    remember(presenter, scope) {
        ScopedSnackbarController(
            presenter = presenter,
            scope = scope
        )
    }
