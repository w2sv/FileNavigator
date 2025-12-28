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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.core.CollectLatestFromFlow
import com.w2sv.composed.core.isLandscapeModeActive
import com.w2sv.composed.material3.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.AnyPresetWrappingFileType
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.LocalNavigator
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.BackArrowTopAppBar
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.screen.navigatorsettings.bottomsheet.FileTypeSelectionBottomSheet
import com.w2sv.filenavigator.ui.screen.navigatorsettings.bottomsheet.rememberFileTypeSelectionState
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.AutoMoveIntroductionDialogIfNotYetShown
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.CustomFileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.CustomFileTypeCreationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.FileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.PresetFileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.NavigatorSettingsList
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.NavigatorConfigActions
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.NavigatorConfigActionsImpl
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.PreviewNavigatorConfigActions
import com.w2sv.filenavigator.ui.util.Easing
import com.w2sv.filenavigator.ui.util.OnVisibilityStateChange
import com.w2sv.filenavigator.ui.util.PreviewOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@Composable
fun NavigatorSettingsScreen(
    navigatorVM: NavigatorSettingsScreenViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    val context = LocalContext.current

    CollectLatestFromFlow(
        flow = navigatorVM.makeSnackbarVisuals,
        key1 = snackbarHostState
    ) { makeSnackbarVisuals ->
        snackbarHostState.dismissCurrentSnackbarAndShow(makeSnackbarVisuals(context))
    }

    AutoMoveIntroductionDialogIfNotYetShown()

    var showFileTypesBottomSheet by rememberSaveable { mutableStateOf(false) }
    var fileTypeConfigurationDialog by rememberSaveable { mutableStateOf<FileTypeConfigurationDialog?>(null) }

    val navigatorConfig by navigatorVM.reversibleConfig.collectAsStateWithLifecycle()
    val configurationHasChanged by navigatorVM.reversibleConfig.statesDissimilar.collectAsStateWithLifecycle()

    NavigatorSettingsScreen(
        navigatorConfig = navigatorConfig,
        navigatorConfigActions = remember(navigatorVM.reversibleConfig) { NavigatorConfigActionsImpl(navigatorVM.reversibleConfig) },
        configurationHasChanged = configurationHasChanged,
        resetConfiguration = navigatorVM.reversibleConfig::reset,
        launchConfigSync = navigatorVM::launchConfigSync,
        showFileTypesBottomSheet = { showFileTypesBottomSheet = true },
        showFileTypeConfigurationDialog = { fileType ->
            fileTypeConfigurationDialog = when (fileType) {
                is AnyPresetWrappingFileType -> FileTypeConfigurationDialog.ConfigurePresetType(fileType)
                is CustomFileType -> FileTypeConfigurationDialog.ConfigureCustomType(fileType)
            }
        }
    )

    if (showFileTypesBottomSheet) {
        FileTypeSelectionBottomSheet(
            state = rememberFileTypeSelectionState(
                navigatorConfig = navigatorConfig,
                toggleSelection = navigatorVM.reversibleConfig::toggleFileTypeEnablement,
                deleteCustomFileType = navigatorVM.reversibleConfig::deleteCustomFileType
            ),
            onDismissRequest = { showFileTypesBottomSheet = false },
            showFileTypeCreationDialog = { fileTypeConfigurationDialog = FileTypeConfigurationDialog.CreateType }
        )
    }

    fileTypeConfigurationDialog?.let { dialog ->
        val closeDialog = { fileTypeConfigurationDialog = null }

        when (dialog) {
            FileTypeConfigurationDialog.CreateType -> CustomFileTypeCreationDialog(
                fileTypes = navigatorConfig.fileTypes.toImmutableSet(),
                onDismissRequest = closeDialog,
                createFileType = navigatorVM.reversibleConfig::createCustomFileType,
                excludeFileExtension = navigatorVM.reversibleConfig::excludeFileExtension
            )

            is FileTypeConfigurationDialog.ConfigureCustomType -> CustomFileTypeConfigurationDialog(
                fileType = dialog.fileType,
                fileTypes = remember { (navigatorConfig.fileTypes - dialog.fileType).toImmutableSet() },
                onDismissRequest = closeDialog,
                saveFileType = { navigatorVM.reversibleConfig.editFileType(dialog.fileType, it) },
                excludeFileExtension = navigatorVM.reversibleConfig::excludeFileExtension
            )

            is FileTypeConfigurationDialog.ConfigurePresetType -> PresetFileTypeConfigurationDialog(
                fileType = dialog.fileType,
                saveFileType = { navigatorVM.reversibleConfig.editFileType(dialog.fileType, it) },
                customFileTypes = navigatorConfig.fileTypes.filterIsInstance<CustomFileType>().toImmutableSet(),
                excludeFileExtension = navigatorVM.reversibleConfig::excludeFileExtension,
                deleteCustomFileType = navigatorVM.reversibleConfig::deleteCustomFileType,
                onDismissRequest = closeDialog
            )
        }
    }
}

@Composable
private fun NavigatorSettingsScreen(
    navigatorConfig: NavigatorConfig,
    navigatorConfigActions: NavigatorConfigActions,
    configurationHasChanged: Boolean,
    resetConfiguration: () -> Unit,
    launchConfigSync: () -> Job,
    showFileTypesBottomSheet: () -> Unit,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    navigator: Navigator = LocalNavigator.current
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var fabButtonRowIsShowing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(id = R.string.navigator_settings),
                onBack = navigator::popBackStack
            )
        },
        floatingActionButton = {
            FabButtonRow(
                configurationHasChanged = configurationHasChanged,
                resetConfiguration = resetConfiguration,
                syncConfiguration = {
                    launchConfigSync()
                        .invokeOnCompletion {
                            // Show 'Applied navigator settings' snackbar only when fab buttons have disappeared
                            scope.launch {
                                snapshotFlow { fabButtonRowIsShowing }
                                    .filter { !it }
                                    .take(1)
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
                },
                onVisibilityStateChange = { fabButtonRowIsShowing = it },
                modifier = Modifier
                    .padding(
                        top = 8.dp, // Snackbar padding
                        end = if (isLandscapeModeActive) 38.dp else 0.dp
                    )
                    .height(70.dp)
            )
        },
        snackbarHost = { AppSnackbarHost() }
    ) { paddingValues ->
        NavigatorSettingsList(
            config = navigatorConfig,
            actions = navigatorConfigActions,
            showFileTypesBottomSheet = showFileTypesBottomSheet,
            showFileTypeConfigurationDialog = showFileTypeConfigurationDialog,
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = Padding.defaultHorizontal)
                .fillMaxSize()
        )
    }
}

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
private fun FabButtonRow(
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
        OnVisibilityStateChange(
            transition = transition,
            callback = onVisibilityStateChange
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FabButton(
                action = FabAction.Reset,
                onClick = resetConfiguration
            )

            FabButton(
                action = FabAction.Apply,
                onClick = syncConfiguration
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

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        NavigatorSettingsScreen(
            navigatorConfig = NavigatorConfig.default,
            navigatorConfigActions = PreviewNavigatorConfigActions,
            configurationHasChanged = true,
            resetConfiguration = {},
            launchConfigSync = { Job() },
            showFileTypesBottomSheet = {},
            showFileTypeConfigurationDialog = {}
        )
    }
}

@Preview
@Composable
private fun PrevDark() {
    PreviewOf(useDarkTheme = true) {
        NavigatorSettingsScreen(
            navigatorConfig = NavigatorConfig.default,
            navigatorConfigActions = PreviewNavigatorConfigActions,
            configurationHasChanged = true,
            resetConfiguration = {},
            launchConfigSync = { Job() },
            showFileTypesBottomSheet = {},
            showFileTypeConfigurationDialog = {}
        )
    }
}
