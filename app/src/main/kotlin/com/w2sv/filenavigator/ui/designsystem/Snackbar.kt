package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.util.CharSequenceText

@Stable
class SnackbarVisibilityState {
    var isVisible by mutableStateOf(false)
}

val LocalSnackbarVisibility = staticCompositionLocalOf { SnackbarVisibilityState() }
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

@Immutable
data class SnackbarAction(val label: String, val callback: () -> Unit)

@Immutable
data class AppSnackbarVisuals(
    override val message: String,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val action: SnackbarAction? = null,
    val kind: SnackbarKind? = null,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals {

    override val actionLabel: String?
        get() = action?.label
}

@Immutable
enum class SnackbarKind(val icon: ImageVector, val iconTint: Color) {
    Success(Icons.Outlined.Check, AppColor.success),
    Error(Icons.Outlined.Warning, AppColor.error)
}

@Composable
fun AppSnackbarHost(modifier: Modifier = Modifier, snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current) {
    SnackbarHost(hostState = snackbarHostState, modifier = modifier) { snackbarData ->
        AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
    }
}

@Composable
fun AppSnackbar(
    visuals: AppSnackbarVisuals,
    modifier: Modifier = Modifier,
    snackbarVisibilityState: SnackbarVisibilityState = LocalSnackbarVisibility.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    Snackbar(
        action = {
            visuals.action?.let { action ->
                TextButton(onClick = action.callback) {
                    Text(text = action.label, color = SnackbarDefaults.actionColor)
                }
            }
        },
        modifier = modifier
    ) {
        DisposableEffect(Unit) {
            snackbarVisibilityState.isVisible = true
            onDispose {
                if (snackbarHostState.currentSnackbarData == null) {
                    snackbarVisibilityState.isVisible = false
                }
            }
        }
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
            Icon(imageVector = it.icon, contentDescription = null, tint = it.iconTint)
            Spacer(modifier = Modifier.width(10.dp))
        }
        CharSequenceText(text = message)
    }
}
