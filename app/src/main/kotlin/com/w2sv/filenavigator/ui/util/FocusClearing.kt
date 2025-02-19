package com.w2sv.filenavigator.ui.util

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.w2sv.composed.CollectFromFlow
import com.w2sv.composed.OnChange
import kotlinx.coroutines.flow.Flow

@Composable
fun ClearFocusOnFlowEmissionOrKeyboardHidden(flow: Flow<Unit>, focusManager: FocusManager = LocalFocusManager.current) {
    CollectFromFlow(flow) { focusManager.clearFocus() }
    ClearFocusOnKeyboardHidden(focusManager = focusManager)
}

@Composable
private fun ClearFocusOnKeyboardHidden(focusManager: FocusManager = LocalFocusManager.current, force: Boolean = false) {
    val keyboardIsVisible = rememberKeyboardIsVisible()

    OnChange(keyboardIsVisible) {
        if (!it) {
            focusManager.clearFocus(force = force)
        }
    }
}

/**
 * https://stackoverflow.com/a/77839533/12083276
 */
@Composable
private fun rememberKeyboardIsVisible(): Boolean {
    var keyboardState by remember { mutableStateOf(false) }
    val view = LocalView.current
    val viewTreeObserver = view.viewTreeObserver
    DisposableEffect(viewTreeObserver) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            keyboardState = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
        }
        viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }
    return keyboardState
}
