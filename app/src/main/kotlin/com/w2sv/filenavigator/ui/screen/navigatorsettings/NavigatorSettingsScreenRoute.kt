package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.core.OnChange
import com.w2sv.domain.model.filetype.AnyPresetWrappingFileType
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.filenavigator.ui.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.screen.navigatorsettings.bottomsheet.FileTypeSelectionBottomSheet
import com.w2sv.filenavigator.ui.screen.navigatorsettings.bottomsheet.rememberFileTypeSelectionState
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.AutoMoveIntroductionDialogIfNotYetShown
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.CustomFileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.CustomFileTypeCreationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.FileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs.PresetFileTypeConfigurationDialog
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.rememberNavigatorConfigActions
import com.w2sv.filenavigator.ui.util.snackbar.dismissCurrentSnackbar
import kotlinx.collections.immutable.toImmutableSet

@Composable
fun NavigatorSettingsScreenRoute(
    navigatorVM: NavigatorSettingsScreenViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    AutoMoveIntroductionDialogIfNotYetShown()

    var showFileTypesBottomSheet by rememberSaveable { mutableStateOf(false) }
    var fileTypeConfigurationDialog by rememberSaveable { mutableStateOf<FileTypeConfigurationDialog?>(null) }

    val navigatorConfig by navigatorVM.reversibleConfig.collectAsStateWithLifecycle()

    OnChange(navigatorConfig) { snackbarHostState.dismissCurrentSnackbar() }

    NavigatorSettingsScreen(
        navigatorConfig = navigatorConfig,
        navigatorConfigActions = rememberNavigatorConfigActions(),
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
