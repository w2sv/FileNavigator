package com.w2sv.filenavigator.ui.screens.navigatorsettings

import android.content.Context
import android.view.animation.AnticipateInterpolator
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.designsystem.showSnackbarAndDismissCurrent
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection.FileTypeSelectionColumn
import com.w2sv.filenavigator.ui.sharedviewmodels.NavigatorViewModel
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.utils.rememberBackPressHandler
import com.w2sv.filenavigator.ui.utils.toEasing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigatorSettingsScreen(
    drawerState: DrawerState,
    returnToHomeScreen: () -> Unit,
    parentScope: CoroutineScope,
    modifier: Modifier = Modifier,
    navigatorVM: NavigatorViewModel = viewModel(),
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    // Reset navigator config on removal from composition, i.e., return to home screen.
    // Doing it here rather than in onBackPress causes the UI not to update shortly before leaving the screen.
    DisposableEffect(Unit) {
        onDispose {
            navigatorVM.configuration.reset()
        }
    }

    val configurationHasChanged by navigatorVM.configuration.statesDissimilar.collectAsStateWithLifecycle()

    val backPressHandler = rememberBackPressHandler(scope)

    val onBackPress: () -> Unit = remember {
        {
            when {
                drawerState.isOpen -> {
                    parentScope.launch { drawerState.close() }
                }

                configurationHasChanged -> {
                    backPressHandler(
                        onFirstPress = {
                            scope.launch {
                                snackbarHostState.showSnackbarAndDismissCurrent(
                                    AppSnackbarVisuals(
                                        message = context.getString(R.string.changes_won_t_be_saved_if_you_leave_now),
                                        kind = SnackbarKind.Error
                                    )
                                )
                            }
                        },
                        onSecondPress = returnToHomeScreen
                    )
                }

                else -> {
                    returnToHomeScreen()
                }
            }
        }
    }

    Column(modifier = modifier) {
        ButtonRow(
            onBackButtonPress = onBackPress,
            configurationHasChanged = configurationHasChanged,
            resetConfiguration = navigatorVM.configuration::reset,
            syncConfiguration = {
                navigatorVM.configuration.launchSync().invokeOnCompletion {
                    parentScope.launch {
                        snackbarHostState.showSnackbarAndDismissCurrent(
                            AppSnackbarVisuals(
                                message = context.getString(R.string.applied_navigator_settings),
                                kind = SnackbarKind.Success
                            )
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.1f)
        )
        Spacer(modifier = Modifier.fillMaxHeight(0.02f))
        FileTypeSelectionColumn(
            navigatorConfiguration = navigatorVM.configuration,
            modifier = Modifier.fillMaxHeight(0.7f)
        )
        Spacer(modifier = Modifier.fillMaxHeight(0.08f))
        MoreColumn(
            disableOnLowBattery = navigatorVM.configuration.disableOnLowBattery.collectAsStateWithLifecycle().value,
            setDisableOnLowBattery = { navigatorVM.configuration.disableOnLowBattery.value = it }
        )
    }

    BackHandler(onBack = onBackPress)
}

@Composable
private fun ButtonRow(
    onBackButtonPress: () -> Unit,
    configurationHasChanged: Boolean,
    resetConfiguration: () -> Unit,
    syncConfiguration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        FilledTonalIconButton(onClick = onBackButtonPress) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = stringResource(R.string.return_to_home_screen)
            )
        }
        AnimatedVisibility(
            visible = configurationHasChanged,
            enter = remember {
                slideInHorizontally(
                    tween(easing = AnticipateInterpolator().toEasing()),
                    initialOffsetX = { it / 2 }
                ) + fadeIn()
            },
            exit = remember {
                slideOutHorizontally(
                    tween(easing = AnticipateInterpolator().toEasing()),
                    targetOffsetX = { it / 2 }
                ) + fadeOut()
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ConfigurationChangeButton(
                    imageVector = Icons.Default.Clear,
                    text = stringResource(R.string.discard),
                    color = AppColor.error,
                    onClick = resetConfiguration
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                ConfigurationChangeButton(
                    imageVector = Icons.Default.Check,
                    text = stringResource(id = R.string.apply),
                    color = AppColor.success,
                    onClick = syncConfiguration
                )
            }
        }
    }
}

@Composable
private fun ConfigurationChangeButton(
    imageVector: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(onClick = onClick, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = imageVector,
                contentDescription = text,
                tint = color
            )
            Text(text = text)
        }
    }
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        ConfigurationChangeButton(
            imageVector = Icons.Default.Clear,
            text = stringResource(id = R.string.discard),
            color = AppColor.error,
            onClick = { }
        )
    }
}

@Composable
private fun MoreColumn(
    disableOnLowBattery: Boolean,
    setDisableOnLowBattery: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.more),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        SwitchItemRow(
            iconRes = R.drawable.ic_battery_low_24,
            textRes = R.string.disable_on_low_battery,
            checked = disableOnLowBattery,
            onCheckedChange = setDisableOnLowBattery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun SwitchItemRow(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = stringResource(id = textRes))
        RightAligned {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}