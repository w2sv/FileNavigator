package com.w2sv.filenavigator.ui.screens.main.components

import android.Manifest
import android.content.Context
import android.os.Build
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.common.utils.powerSaveModeActivated
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.DialogButton
import com.w2sv.filenavigator.ui.components.InfoIcon
import com.w2sv.filenavigator.ui.components.SnackbarAction
import com.w2sv.filenavigator.ui.components.bounceOnClickAnimation
import com.w2sv.filenavigator.ui.components.showSnackbarAndDismissCurrent
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.toEasing
import com.w2sv.navigator.FileNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Immutable
private data class NavigatorButtonProperties(
    val color: Color,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun StartNavigatorButton(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    var showConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                StartNavigatorOnLowBatteryConfirmationDialog(closeDialog = { value = false })
            }
        }

    val startNavigatorOrShowConfirmationDialog = {
        if (mainScreenViewModel.disableListenerOnLowBattery.value && context.powerSaveModeActivated == true) {
            showConfirmationDialog = true
        } else {
            FileNavigator.start(context)
        }
    }
    val permissionState =
        rememberMultiplePermissionsState(permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }) { isGranted ->
            if (isGranted.values.all { it }) {
                startNavigatorOrShowConfirmationDialog()
            }
        }

    var showPermissionsRational by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                PostNotificationsPermissionDialog(
                    onDismissRequest = {
                        value = false
                        mainScreenViewModel.saveShowedPostNotificationsPermissionsRational()
                        permissionState.launchMultiplePermissionRequest()
                    }
                )
            }
        }

    val properties = if (mainScreenViewModel.isNavigatorRunning.collectAsState().value)
        NavigatorButtonProperties(
            AppColor.error,
            R.drawable.ic_stop_24,
            R.string.stop_navigator
        ) { FileNavigator.stop(context) }
    else
        NavigatorButtonProperties(
            AppColor.success,
            R.drawable.ic_start_24,
            R.string.start_navigator
        ) {
            when {
                !permissionState.allPermissionsGranted -> {
                    when {
                        !mainScreenViewModel.showedPostNotificationsPermissionsRational.getValueSynchronously() -> {
                            showPermissionsRational = true
                        }

                        !permissionState.shouldShowRationale -> {
                            scope.launch {
                                mainScreenViewModel.snackbarHostState.showSnackbarAndDismissCurrent(
                                    AppSnackbarVisuals(
                                        message = context.getString(R.string.go_to_the_app_settings_to_grant_the_required_permissions),
                                        action = SnackbarAction(
                                            label = context.getString(R.string.go_to_settings),
                                            callback = { goToAppSettings(context) })
                                    )
                                )
                            }
                        }

                        else -> {
                            permissionState.launchMultiplePermissionRequest()
                        }
                    }
                }

                else -> {
                    startNavigatorOrShowConfirmationDialog()
                }
            }
        }

    val anyStorageAccessGranted by mainScreenViewModel.anyStorageAccessGranted.collectAsState()

    ElevatedButton(
        onClick = properties.onClick,
        modifier = modifier.bounceOnClickAnimation(),
        enabled = anyStorageAccessGranted
    ) {
        AnimatedContent(
            targetState = properties,
            label = "",
            transitionSpec = {
                slideInVertically(
                    animationSpec = tween(
                        durationMillis = DefaultAnimationDuration,
                        easing = AnticipateOvershootInterpolator().toEasing()
                    ),
                    initialOffsetY = { it }) togetherWith
                        slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(
                                durationMillis = DefaultAnimationDuration,
                                easing = AnticipateOvershootInterpolator().toEasing()
                            )
                        )
            }
        ) { properties ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = properties.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (anyStorageAccessGranted) properties.color else AppColor.disabled
                )
                AppFontText(
                    text = stringResource(id = properties.labelRes),
                    fontSize = 18.sp,
                    color = if (anyStorageAccessGranted) MaterialTheme.colorScheme.onBackground else AppColor.disabled
                )
            }
        }
    }
}

@Composable
internal fun PostNotificationsPermissionDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(onClick = onDismissRequest) {
                AppFontText(text = stringResource(id = R.string.understood))
            }
        },
        icon = {
            InfoIcon()
        },
        text = {
            AppFontText(
                text = stringResource(R.string.post_notifications_permission_rational)
            )
        }
    )
}

@Composable
internal fun StartNavigatorOnLowBatteryConfirmationDialog(
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = closeDialog,
        confirmButton = {
            DialogButton(
                onClick = {
                    FileNavigator.start(context)
                    mainScreenViewModel.saveDisableListenerOnLowBattery(false)
                    closeDialog()
                }
            ) {
                AppFontText(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            ElevatedButton(onClick = closeDialog) {
                AppFontText(text = stringResource(R.string.no))
            }
        },
        text = {
            AppFontText(text = stringResource(R.string.start_navigator_confirmation_dialog_text))
        }
    )
}