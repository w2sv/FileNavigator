package com.w2sv.filenavigator.ui.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.flow.StateFlow

/**
 * A shorthand for
 * `StateFlow<T>.collectAsStateWithLifecycle().value`
 */
@SuppressLint("ComposeUnstableReceiver")
@Composable
fun <T> StateFlow<T>.lifecycleAwareStateValue(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext
): T =
    collectAsStateWithLifecycle(lifecycleOwner, minActiveState, context).value
