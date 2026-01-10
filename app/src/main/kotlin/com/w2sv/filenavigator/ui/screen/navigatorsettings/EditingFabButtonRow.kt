package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.CollectLatestFromFlow
import com.w2sv.composed.core.OnChange
import com.w2sv.core.common.R
import com.w2sv.designsystem.Easing
import com.w2sv.filenavigator.ui.LocalSnackbarVisibility
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.util.OnVisibilityStateChange
import com.w2sv.filenavigator.ui.util.snackbar.rememberSnackbarController
import kotlinx.coroutines.flow.first

private enum class EditingFabButton(@StringRes val labelRes: Int, val imageVector: ImageVector) {
    Reset(
        labelRes = R.string.reset,
        imageVector = Icons.Default.Refresh
    ),
    Apply(
        labelRes = R.string.apply,
        imageVector = Icons.Default.Check
    );

    @ReadOnlyComposable
    @Composable
    fun colors(): Pair<Color, Color> =
        when (this) {
            Reset -> colorScheme.surfaceContainerHigh to colorScheme.onSurface
            Apply -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
        }
}

@Composable
fun EditingFabButtonRow(configEditState: ConfigEditState, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var fabButtonsVisible by remember { mutableStateOf(false) }

    CoordinateFabsAndSnackbarVisibility(
        configEditState = configEditState,
        isEditing = isEditing,
        setIsEditing = { isEditing = it },
        fabButtonsVisible = { fabButtonsVisible }
    )

    EditingFabVisibility(
        visible = isEditing,
        modifier = modifier,
        onVisibilityChanged = { fabButtonsVisible = it }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditingFabButton(
                button = EditingFabButton.Reset,
                onClick = configEditState.reset
            )
            EditingFabButton(
                button = EditingFabButton.Apply,
                onClick = configEditState.apply
            )
        }
    }
}

/**
 * Coordinates visibility between the FABs and snackbars.
 *
 * Ensures that FABs and snackbars never overlap by dismissing and waiting for
 * one to fully disappear before showing the other, while reacting to config
 * edits and apply confirmations.
 */
@Composable
private fun CoordinateFabsAndSnackbarVisibility(
    configEditState: ConfigEditState,
    isEditing: Boolean,
    setIsEditing: (Boolean) -> Unit,
    fabButtonsVisible: () -> Boolean
) {
    val snackbarVisibility = LocalSnackbarVisibility.current
    val snackbarController = rememberSnackbarController()

    // manipulate isEditing on changes of configEditState.hasChanges
    OnChange(configEditState.hasChanges()) { hasChanges ->
        when {
            !isEditing && hasChanges -> {
                // Dismiss any snackbar
                snackbarController.dismissCurrent()

                // Wait for snackbar to disappear
                suspendUntil { !snackbarVisibility.isVisible }
                setIsEditing(true)
            }

            isEditing && !hasChanges -> setIsEditing(false)
        }
    }

    // when changes have been applied:
    // 1. set isEditing to false
    // 2. dismiss any currently shown snackbar
    // 3. show confirmation snackbar once fab buttons have disappeared
    CollectLatestFromFlow(configEditState.changesHaveBeenApplied) {
        setIsEditing(false)

        // Dismiss any snackbar
        snackbarController.dismissCurrent()

        // Wait for FABs to disappear
        suspendUntil { !fabButtonsVisible() }

        // Show confirmation snackbar
        snackbarController.show {
            AppSnackbarVisuals(
                message = getString(R.string.applied_navigator_settings),
                kind = SnackbarKind.Success
            )
        }
    }
}

private suspend fun suspendUntil(condition: () -> Boolean) {
    snapshotFlow { condition() }.first { it }
}

@Composable
private fun EditingFabVisibility(
    visible: Boolean,
    onVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it / 2 },
            animationSpec = tween(easing = Easing.Anticipate)
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { it / 2 },
            animationSpec = tween(easing = Easing.Anticipate)
        ) + fadeOut(),
        modifier = modifier
    ) {
        OnVisibilityStateChange(transition, onVisibilityChanged)
        content()
    }
}

@Composable
private fun EditingFabButton(button: EditingFabButton, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (containerColor, contentColor) = button.colors()

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = button.imageVector,
                contentDescription = null
            )
            Text(text = stringResource(button.labelRes))
        }
    }
}
