package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.core.OnChange
import com.w2sv.composed.core.isLandscapeModeActive
import com.w2sv.designsystem.snackbar.LocalSnackbarHostState
import com.w2sv.designsystem.snackbar.dismissCurrentSnackbar
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.filenavigator.ui.navigation.RootScaffoldState
import com.w2sv.filenavigator.ui.screen.navigatorsettings.bottomsheet.FileTypeSelectionBottomSheet
import com.w2sv.filenavigator.ui.screen.navigatorsettings.bottomsheet.rememberFileTypeSelectionState
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.AutoMoveIntroductionDialogIfNotYetShown
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.CustomFileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.CustomFileTypeCreationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.FileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.PresetFileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.rememberNavigatorConfigActions
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.update

@Composable
fun NavigatorSettingsScreenRoute(
    rootScaffoldState: RootScaffoldState,
    navigatorVM: NavigatorSettingsScreenViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    val showAutoMoveIntroduction by navigatorVM.showAutoMoveIntroduction.collectAsStateWithLifecycle()

    AutoMoveIntroductionDialogIfNotYetShown(
        show = showAutoMoveIntroduction,
        onDismissRequest = navigatorVM::dismissAutoMoveIntroduction
    )

    var showFileTypesBottomSheet by rememberSaveable { mutableStateOf(false) }
    var fileTypeConfigurationDialog by rememberSaveable { mutableStateOf<FileTypeConfigurationDialog?>(null) }
    val reversibleConfig = navigatorVM.reversibleConfig

    val navigatorConfig by reversibleConfig.collectAsStateWithLifecycle()
    val configEditState = rememberConfigEditState(navigatorVM)

    OnChange(navigatorConfig) { snackbarHostState.dismissCurrentSnackbar() }
    DisposableEffect(rootScaffoldState, configEditState) {
        val fab: @Composable () -> Unit = { NavigatorSettingsFloatingActionButton(configEditState = configEditState) }
        rootScaffoldState.fab = fab
        onDispose {
            if (rootScaffoldState.fab === fab) {
                rootScaffoldState.fab = null
            }
        }
    }

    NavigatorSettingsScreen(
        navigatorConfig = navigatorConfig,
        navigatorConfigActions = rememberNavigatorConfigActions(navigatorVM),
        showFileTypesBottomSheet = { showFileTypesBottomSheet = true },
        showFileTypeConfigurationDialog = { fileType ->
            fileTypeConfigurationDialog = when (fileType) {
                is FileType.Preset -> FileTypeConfigurationDialog.ConfigurePresetType(fileType)
                is FileType.Custom -> FileTypeConfigurationDialog.ConfigureCustomType(fileType)
            }
        }
    )

    if (showFileTypesBottomSheet) {
        FileTypeSelectionBottomSheet(
            state = rememberFileTypeSelectionState(
                navigatorConfig = navigatorConfig,
                toggleSelection = { fileType -> reversibleConfig.update { it.toggleFileTypeEnablement(fileType) } },
                deleteCustomFileType = { fileType -> reversibleConfig.update { it.deleteCustomFileType(fileType) } }
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
                createFileType = { fileType -> reversibleConfig.update { it.addCustomFileType(fileType) } },
                excludeFileExtension = { fileType, extension -> reversibleConfig.update { it.excludeFileExtension(fileType, extension) } }
            )

            is FileTypeConfigurationDialog.ConfigureCustomType -> CustomFileTypeConfigurationDialog(
                fileType = dialog.fileType,
                fileTypes = remember { (navigatorConfig.fileTypes - dialog.fileType).toImmutableSet() },
                onDismissRequest = closeDialog,
                saveFileType = { fileType -> reversibleConfig.update { it.editFileType(dialog.fileType, fileType) } },
                excludeFileExtension = { fileType, extension -> reversibleConfig.update { it.excludeFileExtension(fileType, extension) } }
            )

            is FileTypeConfigurationDialog.ConfigurePresetType -> PresetFileTypeConfigurationDialog(
                fileType = dialog.fileType,
                saveFileType = { fileType -> reversibleConfig.update { it.editFileType(dialog.fileType, fileType) } },
                customFileTypes = navigatorConfig.fileTypes.filterIsInstance<FileType.Custom>().toImmutableSet(),
                excludeFileExtension = { fileType, extension -> reversibleConfig.update { it.excludeFileExtension(fileType, extension) } },
                deleteCustomFileType = { fileType -> reversibleConfig.update { it.deleteCustomFileType(fileType) } },
                onDismissRequest = closeDialog
            )
        }
    }
}

@Composable
private fun NavigatorSettingsFloatingActionButton(configEditState: ConfigEditState) {
    EditingFabButtonRow(
        configEditState = configEditState,
        modifier = Modifier
            .padding(
                top = 8.dp, // Snackbar padding
                end = if (isLandscapeModeActive) 38.dp else 0.dp
            )
            .height(70.dp)
    )
}
