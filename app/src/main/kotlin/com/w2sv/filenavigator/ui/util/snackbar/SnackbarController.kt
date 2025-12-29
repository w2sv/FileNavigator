package com.w2sv.filenavigator.ui.util.snackbar

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.w2sv.composed.material3.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.filenavigator.ui.LocalSnackbarHostState

/**
 * Wrapper around [SnackbarHostState] that centralizes snackbar showing,
 * replacement, and dismissal using suspending APIs and a [Context]-backed
 * snackbar factory.
 */
@Stable
class SnackbarController(private val snackbarHostState: SnackbarHostState, private val context: Context) {
    suspend fun show(makeSnackbar: Context.() -> SnackbarVisuals) {
        snackbarHostState.showSnackbar(makeSnackbar(context))
    }

    suspend fun showReplacing(makeSnackbar: Context.() -> SnackbarVisuals) {
        snackbarHostState.dismissCurrentSnackbarAndShow(makeSnackbar(context))
    }

    fun dismissCurrent() {
        snackbarHostState.dismissCurrentSnackbar()
    }
}

@Composable
fun rememberSnackbarController(
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    context: Context = LocalContext.current
): SnackbarController =
    remember(snackbarHostState, context) {
        SnackbarController(
            snackbarHostState = snackbarHostState,
            context = context
        )
    }
