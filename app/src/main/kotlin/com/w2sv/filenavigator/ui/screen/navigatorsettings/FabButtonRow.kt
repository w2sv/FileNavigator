package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.CollectLatestFromFlow
import com.w2sv.composed.core.OnChange
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarVisibility
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.util.Easing
import com.w2sv.filenavigator.ui.util.OnVisibilityStateChange
import com.w2sv.filenavigator.ui.util.dismissCurrentSnackbar
import kotlinx.coroutines.flow.first

private enum class FabButton(@StringRes val labelRes: Int, val imageVector: ImageVector) {
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
fun FabButtonRow(configEditState: ConfigEditState, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var fabButtonsVisible by remember { mutableStateOf(false) }

    CoordinateFabsAndSnackbarVisibility(
        configEditState = configEditState,
        isEditing = isEditing,
        setIsEditing = { isEditing = it },
        fabButtonsVisible = { fabButtonsVisible }
    )

    AnimatedVisibility(
        visible = isEditing,
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
        OnVisibilityStateChange(transition) { fabButtonsVisible = it }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FabButton(
                button = FabButton.Reset,
                onClick = configEditState.reset
            )

            FabButton(
                button = FabButton.Apply,
                onClick = configEditState.apply
            )
        }
    }
}

@Composable
private fun CoordinateFabsAndSnackbarVisibility(
    configEditState: ConfigEditState,
    isEditing: Boolean,
    setIsEditing: (Boolean) -> Unit,
    fabButtonsVisible: () -> Boolean
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val snackbarVisibility = LocalSnackbarVisibility.current
    val context = LocalContext.current

    OnChange(configEditState.hasChanges()) { hasChanges ->
        when {
            !isEditing && hasChanges -> {
                // Dismiss any snackbar
                snackbarHostState.dismissCurrentSnackbar()

                // Wait for snackbar to disappear
                suspendUntil { !snackbarVisibility.isVisible }
                setIsEditing(true)
            }

            isEditing && !hasChanges -> setIsEditing(false)
        }
    }

    // when changes have been applied, dismiss any currently shown snackbar and show confirmation
    // snackbar when fab buttons have disappeared
    CollectLatestFromFlow(configEditState.changesHaveBeenApplied) {
        setIsEditing(false)

        // Dismiss any snackbar
        snackbarHostState.dismissCurrentSnackbar()

        // Wait for FABs to disappear
        suspendUntil { !fabButtonsVisible() }

        // Show confirmation snackbar
        snackbarHostState.showSnackbar(
            AppSnackbarVisuals(
                message = context.getString(R.string.applied_navigator_settings),
                kind = SnackbarKind.Success
            )
        )
    }
}

private suspend fun suspendUntil(condition: () -> Boolean) {
    snapshotFlow { condition() }.first { it }
}

@Composable
private fun FabButton(
    button: FabButton,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
