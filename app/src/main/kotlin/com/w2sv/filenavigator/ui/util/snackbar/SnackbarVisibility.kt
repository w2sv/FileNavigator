package com.w2sv.filenavigator.ui.util.snackbar

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Tracks actual snackbar visibility in a Compose hierarchy. [set] should only be called from within the snackbar host.
 */
@Stable
class SnackbarVisibility {
    var isVisible by mutableStateOf(false)
        private set

    internal fun set(value: Boolean) {
        isVisible = value
    }
}
