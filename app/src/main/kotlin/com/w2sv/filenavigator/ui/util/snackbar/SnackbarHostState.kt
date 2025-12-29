package com.w2sv.filenavigator.ui.util.snackbar

import androidx.compose.material3.SnackbarHostState

// TODO: composed
/**
 * Shortcut for `currentSnackbarData?.dismiss()`.
 */
fun SnackbarHostState.dismissCurrentSnackbar() {
    currentSnackbarData?.dismiss()
}
