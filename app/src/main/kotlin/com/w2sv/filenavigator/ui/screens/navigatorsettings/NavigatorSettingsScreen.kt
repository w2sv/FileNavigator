package com.w2sv.filenavigator.ui.screens.navigatorsettings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.w2sv.composed.CollectLatestFromFlow
import com.w2sv.composed.OnDispose
import com.w2sv.composed.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection.FileTypeAccordion
import com.w2sv.filenavigator.ui.sharedviewmodels.NavigatorViewModel
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.Easing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavigatorSettingsScreen(
    returnToHomeScreen: () -> Unit,
    parentScope: CoroutineScope,
    modifier: Modifier = Modifier,
    navigatorVM: NavigatorViewModel = viewModel(),
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    // Reset navigator config on removal from composition, i.e., return to home screen.
    // Doing it here rather than in onBackPress causes the UI not to update shortly before leaving the screen.
    OnDispose(
        remember {
            {
                navigatorVM.configuration.unconfirmedStates.reset()
            }
        }
    )

    val configurationHasChanged by navigatorVM.configuration.unconfirmedStates.statesDissimilar.collectAsStateWithLifecycle()

    CollectLatestFromFlow(
        flow = navigatorVM.makeSnackbarVisuals,
        action = { makeSnackbarVisuals ->
            snackbarHostState.dismissCurrentSnackbarAndShow(makeSnackbarVisuals(context))
        },
        key1 = snackbarHostState
    )

    BackHandler(onBack = returnToHomeScreen)

    Column(modifier = modifier) {
        ButtonRow(
            onBackButtonPress = returnToHomeScreen,
            configurationHasChanged = configurationHasChanged,
            resetConfiguration = navigatorVM.configuration.unconfirmedStates::reset,
            syncConfiguration = {
                navigatorVM.configuration.unconfirmedStates.launchSync().invokeOnCompletion {
                    parentScope.launch {
                        snackbarHostState.dismissCurrentSnackbarAndShow(
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
                .height(56.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val firstDisabledFileType by navigatorVM.configuration.firstDisabledFileType.collectAsStateWithLifecycle()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.file_types),
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
            items(navigatorVM.configuration.sortedFileTypes, key = { it }) { fileType ->
                i { "Laying out ${fileType.name}" }

                FileTypeAccordion(
                    fileType = fileType,
                    isEnabled = navigatorVM.configuration.fileEnablementMap.getValue(fileType),
                    isFirstDisabled = remember {
                        derivedStateOf {
                            fileType == firstDisabledFileType
                        }
                    }.value,
                    onCheckedChange = remember(fileType) {
                        {
                            navigatorVM.configuration.onFileTypeCheckedChange(
                                fileType = fileType,
                                checkedNew = it
                            )
                        }
                    },
                    mediaFileSourceEnabled = remember(fileType) {
                        {
                            navigatorVM.configuration.mediaFileSourceEnablementMap.getOrDefault(
                                it,
                                true
                            )
                        }
                    },
                    onMediaFileSourceCheckedChange = remember(fileType) {
                        { source, checked ->
                            navigatorVM.configuration.onMediaFileSourceCheckedChange(
                                source,
                                checked
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItemPlacement(tween(DefaultAnimationDuration))  // Animate upon reordering
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MoreColumn(
                    disableOnLowBattery = navigatorVM.configuration.disableOnLowBattery.collectAsStateWithLifecycle().value,
                    setDisableOnLowBattery = {
                        navigatorVM.configuration.disableOnLowBattery.value = it
                    }
                )
            }
        }
    }
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
                    tween(easing = Easing.Anticipate),
                    initialOffsetX = { it / 2 }
                ) + fadeIn()
            },
            exit = remember {
                slideOutHorizontally(
                    tween(easing = Easing.Anticipate),
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