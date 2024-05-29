package com.w2sv.filenavigator.ui.screens.navigatorsettings

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.w2sv.composed.CollectLatestFromFlow
import com.w2sv.composed.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.composed.isLandscapeModeActive
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.NavigationTransitions
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.designsystem.TopAppBarAboveHorizontalDivider
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.AddFileTypesBottomSheet
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.NavigatorConfigurationColumn
import com.w2sv.filenavigator.ui.sharedviewmodels.NavigatorViewModel
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.utils.Easing
import com.w2sv.filenavigator.ui.utils.activityViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(style = NavigationTransitions::class)
@Composable
fun NavigatorSettingsScreen(
    navigator: DestinationsNavigator,
    navigatorVM: NavigatorViewModel = activityViewModel(),
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    val configurationHasChanged by navigatorVM.configuration.unconfirmedStates.statesDissimilar.collectAsStateWithLifecycle()

    CollectLatestFromFlow(
        flow = navigatorVM.makeSnackbarVisuals,
        key1 = snackbarHostState
    ) { makeSnackbarVisuals ->
        snackbarHostState.dismissCurrentSnackbarAndShow(makeSnackbarVisuals(context))
    }

    val onBack: () -> Unit = remember {
        {
            navigatorVM.configuration.unconfirmedStates.reset()
            navigator.popBackStack()
        }
    }

    BackHandler(onBack = onBack)

    val scope = rememberCoroutineScope()
    var showAddFileTypesBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBarAboveHorizontalDivider(
                title = stringResource(id = R.string.navigator_settings),
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.return_to_main_screen)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ConfigurationButtonRow(
                configurationHasChanged = configurationHasChanged,
                resetConfiguration = remember { { navigatorVM.configuration.unconfirmedStates.reset() } },
                syncConfiguration = remember {
                    {
                        navigatorVM.configuration.unconfirmedStates.launchSync()
                            .invokeOnCompletion {
                                scope.launch {
                                    delay(500)  // Wait until fab button row has disappeared
                                    snackbarHostState.dismissCurrentSnackbarAndShow(
                                        AppSnackbarVisuals(
                                            message = context.getString(R.string.applied_navigator_settings),
                                            kind = SnackbarKind.Success
                                        )
                                    )
                                }
                            }
                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp, end = if (isLandscapeModeActive) 38.dp else 0.dp)
                    .height(70.dp)
            )
        },
        snackbarHost = {
            AppSnackbarHost()
        }
    ) { paddingValues ->
        NavigatorConfigurationColumn(
            configuration = navigatorVM.configuration,
            showAddFileTypesBottomSheet = remember { { showAddFileTypesBottomSheet = true } },
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = if (isPortraitModeActive) Padding.defaultHorizontal else 52.dp)
                .fillMaxSize()
        )

        if (showAddFileTypesBottomSheet) {
            AddFileTypesBottomSheet(
                disabledFileTypes = navigatorVM.configuration.disabledFileTypes.toPersistentList(),
                addFileTypes = remember {
                    {
                        it.forEach { fileType ->
                            navigatorVM.configuration.onFileTypeCheckedChange(fileType, true)
                        }
                    }
                },
                onDismissRequest = remember { { showAddFileTypesBottomSheet = false } }
            )
        }
    }
}

@Composable
private fun ConfigurationButtonRow(
    configurationHasChanged: Boolean,
    resetConfiguration: () -> Unit,
    syncConfiguration: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        },
        modifier = modifier
    ) {
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