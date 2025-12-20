package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.core.CollectLatestFromFlow
import com.w2sv.composed.material3.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.AnyPresetWrappingFileType
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.BackArrowTopAppBar
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.navigation.LocalNavigator
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.AutoMoveIntroductionDialogIfNotYetShown
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.FileTypeSelectionBottomSheetContent
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.NavigatorConfigurationColumn
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration.CustomFileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration.CustomFileTypeCreationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration.FileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration.PresetFileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.rememberFileTypeSelectionState
import com.w2sv.filenavigator.ui.state.ReversibleNavigatorConfig
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.Easing
import com.w2sv.filenavigator.ui.util.OnVisibilityStateChange
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
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

    var fileTypeConfigurationDialog by rememberSaveable { mutableStateOf<FileTypeConfigurationDialog?>(null) }

    val navigatorConfig by navigatorVM.reversibleConfig.collectAsStateWithLifecycle()
    val configurationHasChanged by navigatorVM.reversibleConfig.statesDissimilar.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true).apply { rememberCoroutineScope().launch { expand() } }
    val scaffoldState = rememberBottomSheetScaffoldState(snackbarHostState = snackbarHostState, bottomSheetState = bottomSheetState)

    NavigatorSettingsScreen(
        navigatorConfig = navigatorConfig,
        reversibleNavigatorConfig = navigatorVM.reversibleConfig,
        scaffoldState = scaffoldState,
        configurationHasChanged = configurationHasChanged,
        resetConfiguration = navigatorVM.reversibleConfig::reset,
        launchConfigSync = navigatorVM::launchConfigSync,
        showFileTypeConfigurationDialog = { fileType ->
            fileTypeConfigurationDialog = when (fileType) {
                is AnyPresetWrappingFileType -> FileTypeConfigurationDialog.ConfigurePresetType(fileType)
                is CustomFileType -> FileTypeConfigurationDialog.ConfigureCustomType(fileType)
            }
        },
        sheetContent = {
            FileTypeSelectionBottomSheetContent(
                rememberFileTypeSelectionState(
                    navigatorConfig = navigatorConfig,
                    toggleSelection = navigatorVM.reversibleConfig::toggleFileTypeEnablement,
                    deleteCustomFileType = navigatorVM.reversibleConfig::deleteCustomFileType
                ),
                showFileTypeCreationDialog = { fileTypeConfigurationDialog = FileTypeConfigurationDialog.CreateType }
            )
        }
    )

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
    reversibleNavigatorConfig: ReversibleNavigatorConfig,
    scaffoldState: BottomSheetScaffoldState,
    configurationHasChanged: Boolean,
    resetConfiguration: () -> Unit,
    launchConfigSync: () -> Job,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    navigator: Navigator = LocalNavigator.current
) {
    val scope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(id = R.string.navigator_settings),
                onBack = navigator::popBackStack
            )
        },
        snackbarHost = { AppSnackbarHost() },
        sheetContent = sheetContent,
        sheetContainerColor = colorScheme.surfaceContainerLowest,
//        sheetTonalElevation = 32.dp,
//        sheetShadowElevation = 32.dp
    ) { paddingValues ->
        NavigatorConfigurationColumn(
            config = navigatorConfig,
            reversibleConfig = reversibleNavigatorConfig,
            showFileTypesBottomSheet = { scope.launch { scaffoldState.bottomSheetState.expand() } },
            showFileTypeConfigurationDialog = showFileTypeConfigurationDialog,
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = Padding.defaultHorizontal)
                .fillMaxSize()
        )
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
                contentDescription = text
            )
            Text(text = text)
        }
    }
}

@Preview
@Composable
private fun ConfigurationFABButtonPrev() {
    AppTheme {
        ConfigurationFABButton(
            imageVector = Icons.Default.Clear,
            text = stringResource(id = R.string.discard),
            onClick = { }
        )
    }
}
