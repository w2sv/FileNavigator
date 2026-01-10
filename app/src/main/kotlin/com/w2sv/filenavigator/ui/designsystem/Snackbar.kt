package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.OnDispose
import com.w2sv.designsystem.CharSequenceText
import com.w2sv.designsystem.theme.ResultColor
import com.w2sv.filenavigator.ui.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.LocalSnackbarVisibility
import com.w2sv.filenavigator.ui.util.snackbar.SnackbarVisibility

/**
 * App-specific extension of [SnackbarVisuals], adding [kind] and [action].
 */
@Immutable
data class AppSnackbarVisuals(
    override val message: String,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val action: SnackbarAction? = null,
    val kind: SnackbarKind? = null,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals {
    override val actionLabel: String? = action?.label
}

data class SnackbarAction(val label: String, val callback: () -> Unit)

enum class SnackbarKind(val icon: ImageVector, val iconTint: Color) {
    Success(Icons.Outlined.Check, ResultColor.success),
    Error(Icons.Outlined.Warning, ResultColor.error)
}

@Composable
fun AppSnackbarHost(modifier: Modifier = Modifier, snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current) {
    SnackbarHost(hostState = snackbarHostState, modifier = modifier) { snackbarData ->
        UpdateSnackbarVisibility(snackbarHostState = snackbarHostState)
        AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
    }
}

@Composable
fun AppSnackbar(visuals: AppSnackbarVisuals, modifier: Modifier = Modifier) {
    Snackbar(
        action = {
            visuals.action?.let {
                TextButton(onClick = it.callback) {
                    Text(text = it.label, color = SnackbarDefaults.actionColor)
                }
            }
        },
        modifier = modifier
    ) {
        AppSnackbarContent(visuals.kind, visuals.message)
    }
}

@Composable
fun AppSnackbarContent(
    snackbarKind: SnackbarKind?,
    message: CharSequence,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier, horizontalArrangement = horizontalArrangement) {
        snackbarKind?.let {
            Icon(imageVector = it.icon, contentDescription = null, tint = it.iconTint, modifier = Modifier.padding(end = 10.dp))
        }
        CharSequenceText(text = message)
    }
}

@Composable
private fun UpdateSnackbarVisibility(
    snackbarVisibility: SnackbarVisibility = LocalSnackbarVisibility.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    SideEffect { snackbarVisibility.set(true) }
    OnDispose {
        // Checking whether currentSnackbarData is necessary in cases where snackbar a is immediately replaced by snackbar b, where we don't
        // want a sequence of true -> false -> true, but just true.
        if (snackbarHostState.currentSnackbarData == null) {
            snackbarVisibility.set(false)
        }
    }
}
