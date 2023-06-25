package com.w2sv.filenavigator.ui.screens.main

import android.Manifest
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.PreferencesKey
import com.w2sv.filenavigator.navigator.service.FileNavigatorService
import com.w2sv.filenavigator.ui.AppFontText
import com.w2sv.filenavigator.ui.ExtendedSnackbarVisuals
import com.w2sv.filenavigator.ui.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.filenavigator.ui.theme.disabledColor
import com.w2sv.filenavigator.ui.theme.md_negative
import com.w2sv.filenavigator.ui.theme.md_positive
import com.w2sv.filenavigator.utils.powerSaveModeActivated
import kotlinx.coroutines.launch

private data class NavigatorButtonProperties(
    val color: Color,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun StartNavigatorButton(
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isNavigatorRunning by mainScreenViewModel.isNavigatorRunning.collectAsState()

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
            FileNavigatorService.start(context)
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
                        scope.launch {
                            mainScreenViewModel.dataStoreRepository.save(
                                PreferencesKey.SHOWED_POST_NOTIFICATIONS_PERMISSION_RATIONAL,
                                true
                            )
                        }
                        permissionState.launchMultiplePermissionRequest()
                    }
                )
            }
        }

    val stopConfigProperties = NavigatorButtonProperties(
        md_negative,
        R.drawable.ic_stop_24,
        R.string.stop_navigator
    ) { FileNavigatorService.stop(context) }
    val startConfigProperties = NavigatorButtonProperties(
        md_positive,
        R.drawable.ic_start_24,
        R.string.start_navigator
    ) {
        when {
            !permissionState.allPermissionsGranted -> {
                when {
                    !mainScreenViewModel.dataStoreRepository.showedPostNotificationsPermissionsRational.getValueSynchronously() -> {
                        showPermissionsRational = true
                    }

                    !permissionState.shouldShowRationale -> {
                        scope.launch {
                            mainScreenViewModel.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                ExtendedSnackbarVisuals(
                                    message = context.getString(R.string.go_to_the_app_settings_to_grant_the_required_permissions),
                                    actionLabel = context.getString(R.string.go_to_settings),
                                    action = { goToAppSettings(context) }
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

    val properties = if (isNavigatorRunning) stopConfigProperties else startConfigProperties

    val anyStorageAccessGranted by mainScreenViewModel.anyStorageAccessGranted.collectAsState()

    val pagerState = rememberPagerState()

    LaunchedEffect(Unit) {
        mainScreenViewModel.isNavigatorRunning.collect {
            if (it) {
                pagerState.animateScrollToPage(1)
            } else {
                pagerState.animateScrollToPage(0)
            }
        }
    }

    ElevatedButton(
        onClick = properties.onClick,
        modifier = modifier.bounceClick(),
        enabled = anyStorageAccessGranted
    ) {
        AnimatedVisibility(visible = isNavigatorRunning) {

        }
        HorizontalPager(pageCount = 2, userScrollEnabled = false, state = pagerState) {
            ButtonContent(
                properties = if (it == 0) startConfigProperties else stopConfigProperties,
                anyStorageAccessGranted = anyStorageAccessGranted
            )
        }
    }
}

@Composable
private fun ButtonContent(properties: NavigatorButtonProperties, anyStorageAccessGranted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            painter = painterResource(id = properties.iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = if (anyStorageAccessGranted) properties.color else disabledColor()
        )
        AppFontText(
            text = stringResource(id = properties.labelRes),
            fontSize = 18.sp,
            color = if (anyStorageAccessGranted) MaterialTheme.colorScheme.onBackground else disabledColor()
        )
    }
}

enum class ButtonState { Pressed, Idle }

fun Modifier.bounceClick(minScale: Float = 0.8f) = composed {
    var buttonState by remember { mutableStateOf(ButtonState.Idle) }
    val scale by animateFloatAsState(
        if (buttonState == ButtonState.Pressed) minScale else 1f,
        label = ""
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
//        .clickable(
//            interactionSource = remember { MutableInteractionSource() },
//            indication = null,
//            onClick = { }
//        )
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonState.Pressed
                }
            }
        }
}
