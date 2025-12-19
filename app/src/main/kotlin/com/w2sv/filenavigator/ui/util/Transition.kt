package com.w2sv.filenavigator.ui.util

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import com.w2sv.composed.core.OnChange
import com.w2sv.composed.core.OnDispose

@Composable
fun OnVisibilityStateChange(transition: Transition<*>, callback: (Boolean) -> Unit) {
    OnChange(transition.targetState) {
        if (it != EnterExitState.PostExit) {
            callback(true)
        }
    }
    OnDispose {
        callback(false)
    }
}
