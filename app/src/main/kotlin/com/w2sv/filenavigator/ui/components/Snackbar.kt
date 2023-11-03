package com.w2sv.filenavigator.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.ui.theme.AppColor

val LocalSnackbarHostState = compositionLocalOf { SnackbarHostState() }

@Immutable
data class SnackbarAction(val label: String, val callback: () -> Unit)

data class AppSnackbarVisuals(
    override val message: String,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val action: SnackbarAction? = null,
    val kind: SnackbarKind? = null,
    override val withDismissAction: Boolean = false,
) : SnackbarVisuals {

    override val actionLabel: String?
        get() = action?.label
}

sealed class SnackbarKind(val icon: ImageVector, val iconTint: Color) {
    data object Success : SnackbarKind(Icons.Outlined.Check, AppColor.success)
    data object Error : SnackbarKind(Icons.Outlined.Warning, AppColor.error)
}

suspend fun SnackbarHostState.showSnackbarAndDismissCurrent(snackbarVisuals: SnackbarVisuals) {
    currentSnackbarData?.dismiss()
    showSnackbar(snackbarVisuals)
}

@Composable
fun AppSnackbar(visuals: AppSnackbarVisuals) {
    Snackbar(
        action = {
            visuals.action?.let { action ->
                TextButton(
                    onClick = action.callback,
                ) {
                    AppFontText(text = action.label, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            visuals.kind?.let { kind ->
                Icon(imageVector = kind.icon, contentDescription = null, tint = kind.iconTint)
                Spacer(modifier = Modifier.width(10.dp))
            }
            AppFontText(text = visuals.message)
        }
    }
}