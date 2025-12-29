package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.w2sv.filenavigator.ui.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.BackArrowTopAppBar
import com.w2sv.filenavigator.ui.designsystem.Padding
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
import com.w2sv.filenavigator.ui.util.PreviewOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.emptyFlow

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

    NavigatorSettingsScreen(
        navigatorConfig = navigatorConfig,
        navigatorConfigActions = remember(navigatorVM.reversibleConfig) { NavigatorConfigActionsImpl(navigatorVM.reversibleConfig) },
        configEditState = rememberConfigEditState(navigatorVM),
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
    configEditState: ConfigEditState,
    showFileTypesBottomSheet: () -> Unit,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    navigator: Navigator = LocalNavigator.current
) {
    Scaffold(
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(id = R.string.navigator_settings),
                onBack = navigator::popBackStack
            )
        },
        floatingActionButton = {
            EditingFabButtonRow(
                configEditState = configEditState,
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

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        NavigatorSettingsScreen(
            navigatorConfig = NavigatorConfig.default,
            navigatorConfigActions = PreviewNavigatorConfigActions,
            configEditState = ConfigEditState(
                hasChanges = { true },
                reset = {},
                apply = {},
                changesHaveBeenApplied = emptyFlow()
            ),
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
            configEditState = ConfigEditState(
                hasChanges = { true },
                reset = {},
                apply = {},
                changesHaveBeenApplied = emptyFlow()
            ),
            showFileTypesBottomSheet = {},
            showFileTypeConfigurationDialog = {}
        )
    }
}
