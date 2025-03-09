package com.w2sv.filenavigator.ui.designsystem

import android.annotation.SuppressLint
import androidx.compose.foundation.BasicTooltipDefaults.GlobalMutatorMutex
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatorMutex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipScope
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("ComposeUnstableReceiver")
@Composable
fun TooltipScope.DeletionTooltip(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    PlainTooltip(caretSize = TooltipDefaults.caretSize, tonalElevation = 4.dp, shadowElevation = 4.dp, modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = contentDescription
            )
        }
    }
}

@Stable
class ExtendedTooltipState(state: TooltipState, val showTooltip: () -> Unit) : TooltipState by state

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberExtendedTooltipState(
    initialIsVisible: Boolean = false,
    isPersistent: Boolean = true,
    mutatorMutex: MutatorMutex = GlobalMutatorMutex,
    scope: CoroutineScope = rememberCoroutineScope()
): ExtendedTooltipState {
    return remember(initialIsVisible, isPersistent, mutatorMutex) {
        val state = TooltipState(initialIsVisible, isPersistent, mutatorMutex)
        ExtendedTooltipState(state = state, showTooltip = { scope.launch { state.show() } })
    }
}
