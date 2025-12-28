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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.material3.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.util.Easing
import com.w2sv.filenavigator.ui.util.OnVisibilityStateChange
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private enum class FabAction(@StringRes val labelRes: Int, val imageVector: ImageVector) {
    Reset(
        labelRes = R.string.reset,
        imageVector = Icons.Default.Refresh
    ),
    Apply(
        labelRes = R.string.apply,
        imageVector = Icons.Default.Check
    );

    val containerColor
        @ReadOnlyComposable
        @Composable
        get() = when (this) {
            Reset -> colorScheme.surfaceContainerHigh
            Apply -> colorScheme.primaryContainer
        }

    val contentColor
        @ReadOnlyComposable
        @Composable
        get() = when (this) {
            Reset -> colorScheme.onSurface
            Apply -> colorScheme.onPrimaryContainer
        }
}

@Composable
fun FabButtonRow(
    configEditState: ConfigEditState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    var isShowing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AnimatedVisibility(
        visible = configEditState.hasChanges(),
        enter = remember {
            slideInHorizontally(
                animationSpec = tween(easing = Easing.Anticipate),
                initialOffsetX = { it / 2 }
            ) + fadeIn()
        },
        exit = remember {
            slideOutHorizontally(
                animationSpec = tween(easing = Easing.Anticipate),
                targetOffsetX = { it / 2 }
            ) + fadeOut()
        },
        modifier = modifier
    ) {
        OnVisibilityStateChange(
            transition = transition,
            callback = { isShowing = it }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FabButton(
                action = FabAction.Reset,
                onClick = configEditState.reset
            )

            FabButton(
                action = FabAction.Apply,
                onClick = {
                    configEditState.apply()
                    scope.launch {
                        configEditState.changesHaveBeenApplied
                            .collect {
                                snapshotFlow { isShowing }.first { !it }

                                snackbarHostState.dismissCurrentSnackbarAndShow(
                                    AppSnackbarVisuals(
                                        message = context.getString(R.string.applied_navigator_settings),
                                        kind = SnackbarKind.Success
                                    )
                                )
                            }
                    }
                }
            )
        }
    }
}

@Composable
private fun FabButton(
    action: FabAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = action.containerColor,
        contentColor = action.contentColor
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = action.imageVector,
                contentDescription = null
            )
            Text(text = stringResource(action.labelRes))
        }
    }
}
