package com.w2sv.filenavigator.ui.util

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.w2sv.composed.material3.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
open class SnackbarEmitter(val snackbarHostState: SnackbarHostState, private val context: Context) {
    fun dismissCurrentAndShow(scope: CoroutineScope, makeSnackbar: Context.() -> SnackbarVisuals) {
        scope.launch { dismissCurrentAndShowSuspending(makeSnackbar) }
    }

    suspend fun dismissCurrentAndShowSuspending(makeSnackbar: Context.() -> SnackbarVisuals) {
        snackbarHostState.dismissCurrentSnackbarAndShow(context.run { makeSnackbar() })
    }

    fun dismissCurrent() {
        snackbarHostState.currentSnackbarData?.dismiss()
    }
}

@Composable
fun rememberSnackbarEmitter(
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    context: Context = LocalContext.current
): SnackbarEmitter =
    remember(snackbarHostState) {
        SnackbarEmitter(
            snackbarHostState = snackbarHostState,
            context = context
        )
    }

@Composable
fun rememberScopedSnackbarEmitter(
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
): ScopedSnackbarEmitter =
    remember(snackbarHostState, scope) {
        ScopedSnackbarEmitter(
            snackbarHostState = snackbarHostState,
            scope = scope,
            context = context
        )
    }

@Stable
class ScopedSnackbarEmitter(snackbarHostState: SnackbarHostState, context: Context, private val scope: CoroutineScope) :
    SnackbarEmitter(snackbarHostState, context) {

    fun dismissCurrentAndShow(makeSnackbar: Context.() -> SnackbarVisuals) {
        dismissCurrentAndShow(scope, makeSnackbar)
    }
}
