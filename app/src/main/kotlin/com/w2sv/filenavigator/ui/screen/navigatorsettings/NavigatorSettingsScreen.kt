package com.w2sv.filenavigator.ui.screen.navigatorsettings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.w2sv.composed.CollectLatestFromFlow
import com.w2sv.composed.OnChange
import com.w2sv.composed.OnDispose
import com.w2sv.composed.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.composed.isLandscapeModeActive
import com.w2sv.composed.rememberStyledTextResource
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.BackArrowTopAppBar
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.NavigationTransitions
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.AddFileTypesBottomSheet
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.NavigatorConfigurationColumn
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.Easing
import com.w2sv.filenavigator.ui.util.activityViewModel
import com.w2sv.filenavigator.ui.viewmodel.AppViewModel
import com.w2sv.filenavigator.ui.viewmodel.NavigatorViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(style = NavigationTransitions::class)
@Composable
fun NavigatorSettingsScreen(
    navigator: DestinationsNavigator,
    navigatorVM: NavigatorViewModel = activityViewModel(),
    appVM: AppViewModel = activityViewModel(),
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    if (rememberShowAutoMoveIntroductionWithDelay(showAutoMoveIntroduction = appVM.showAutoMoveIntroduction.collectAsStateWithLifecycle().value)) {
        AutoMoveIntroductionDialog(
            onDismissRequest = remember {
                {
                    appVM.saveShowAutoMoveIntroduction(
                        false
                    )
                }
            }
        )
    }

    CollectLatestFromFlow(
        flow = navigatorVM.makeSnackbarVisuals,
        key1 = snackbarHostState
    ) { makeSnackbarVisuals ->
        snackbarHostState.dismissCurrentSnackbarAndShow(makeSnackbarVisuals(context))
    }

    val navigatorConfigHasChanged by navigatorVM.reversibleConfig.statesDissimilar.collectAsStateWithLifecycle()

    var fabButtonRowIsShowing by remember {
        mutableStateOf(false)
    }

    val onBack: () -> Unit = remember {
        {
            navigatorVM.reversibleConfig.reset()
            navigator.popBackStack()
        }
    }

    BackHandler(onBack = onBack)

    var showAddFileTypesBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(id = R.string.navigator_settings),
                onBack = onBack
            )
        },
        floatingActionButton = {
            ConfigurationButtonRow(
                configurationHasChanged = navigatorConfigHasChanged,
                resetConfiguration = remember { { navigatorVM.reversibleConfig.reset() } },
                syncConfiguration = remember {
                    {
                        navigatorVM.viewModelScope.launch {
                            navigatorVM.reversibleConfig.sync()
                        }
                            .invokeOnCompletion {
                                scope.launch {
                                    snapshotFlow { fabButtonRowIsShowing }.filter { !it }.take(1)
                                        .collect {
                                            snackbarHostState.dismissCurrentSnackbarAndShow(
                                                AppSnackbarVisuals(
                                                    message = context.getString(R.string.applied_navigator_settings),
                                                    kind = SnackbarKind.Success
                                                )
                                            )
                                        }
                                }
                            }
                    }
                },
                onVisibilityStateChange = {
                    fabButtonRowIsShowing = it
                },
                modifier = Modifier
                    .padding(
                        top = 8.dp,  // Snackbar padding
                        end = if (isLandscapeModeActive) 38.dp else 0.dp
                    )
                    .height(70.dp)
            )
        },
        snackbarHost = {
            AppSnackbarHost()
        }
    ) { paddingValues ->
        NavigatorConfigurationColumn(
            reversibleConfig = navigatorVM.reversibleConfig,
            showAddFileTypesBottomSheet = remember { { showAddFileTypesBottomSheet = true } },
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = Padding.defaultHorizontal)
                .fillMaxSize()
        )

        if (showAddFileTypesBottomSheet) {
            AddFileTypesBottomSheet(
                disabledFileTypes = navigatorVM.reversibleConfig.collectAsStateWithLifecycle().value.disabledFileTypes.toPersistentList(),
                addFileTypes = remember {
                    {
                        it.forEach { fileType ->
                            navigatorVM.reversibleConfig.onFileTypeCheckedChange(fileType, true)
                        }
                    }
                },
                onDismissRequest = remember { { showAddFileTypesBottomSheet = false } }
            )
        }
    }
}

@Composable
private fun rememberShowAutoMoveIntroductionWithDelay(showAutoMoveIntroduction: Boolean): Boolean {
    var showAutoMoveIntroductionWithDelay by remember {
        mutableStateOf(false)
    }
    OnChange(value = showAutoMoveIntroduction) {
        if (it) {
            delay(500)
        }
        showAutoMoveIntroductionWithDelay = it
    }
    return showAutoMoveIntroductionWithDelay
}

@Composable
private fun AutoMoveIntroductionDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.introducing_auto_move)) },
        icon = { Text(text = "🎉", fontSize = 30.sp) },
        confirmButton = {
            DialogButton(
                text = stringResource(R.string.awesome),
                onClick = onDismissRequest
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.you_can_now_enable_file_source_specific_auto_moving_by),
                    modifier = Modifier.padding(bottom = TextSectionBottomPadding)
                )
                Text(
                    text = rememberStyledTextResource(R.string._1_clicking_on_the_auto_button_of_an_enabled_file_source_2_selecting_a_destination_3_saving_the_changes),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = TextSectionBottomPadding)
                )
                Text(
                    text = stringResource(R.string.now_whenever_a_new_file_corresponding_to_the_file_source_is_discovered_it_will_be_automatically_moved_to_the_selected_destination_without_you_needing_to_do_anything_else)
                )
            }
        },
        modifier = modifier
    )
}

private val TextSectionBottomPadding = 6.dp

@Preview
@Composable
private fun AutoMoveIntroductionPrev() {
    AppTheme {
        AutoMoveIntroductionDialog(onDismissRequest = {})
    }
}

@Composable
private fun ConfigurationButtonRow(
    configurationHasChanged: Boolean,
    resetConfiguration: () -> Unit,
    syncConfiguration: () -> Unit,
    onVisibilityStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = configurationHasChanged,
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
        OnVisibilityStateChange(transition = transition, callback = onVisibilityStateChange)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConfigurationFABButton(
                imageVector = Icons.Default.Refresh,
                text = stringResource(R.string.reset),
                onClick = resetConfiguration
            )
            ConfigurationFABButton(
                imageVector = Icons.Default.Check,
                text = stringResource(id = R.string.apply),
                onClick = syncConfiguration
            )
        }
    }
}

@Composable
@Stable
fun OnVisibilityStateChange(transition: Transition<*>, callback: (Boolean) -> Unit) {
    OnChange(transition.targetState) {
        if (it != EnterExitState.PostExit) {
            callback(true)
        }
    }
    OnDispose {
        callback(false)
    }
}

@Composable
private fun ConfigurationFABButton(
    imageVector: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = imageVector,
                contentDescription = text,
            )
            Text(text = text)
        }
    }
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        ConfigurationFABButton(
            imageVector = Icons.Default.Clear,
            text = stringResource(id = R.string.discard),
            onClick = { }
        )
    }
}