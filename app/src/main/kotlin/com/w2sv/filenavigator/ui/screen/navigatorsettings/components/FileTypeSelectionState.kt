package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.model.navigatorconfig.sortedByOrdinal
import com.w2sv.kotlinutils.coroutines.launchDelayed
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

@Suppress("ConstPropertyName")
private const val SelectionWarningDuration = 3_000L

@Stable
class FileTypeSelectionState(
    val selectionMap: ImmutableMap<FileType, Boolean>,
    private val toggleSelection: (FileType) -> Unit,
    val deleteCustomFileType: (CustomFileType) -> Unit,
    private val scope: CoroutineScope
) {
    val sortedFileTypes: List<FileType> by threadUnsafeLazy {
        selectionMap.keys.sortedByOrdinal()
    }

    // --- Warning state ---

    var showSelectionWarning by mutableStateOf(false)
        private set

    private var warningResetJob: Job? = null

    private val isSingleFileTypeEnabled: Boolean
        get() = selectionMap.values.count { it } == 1

    private fun cannotDeselect(fileType: FileType): Boolean =
        selectionMap.getValue(fileType) && isSingleFileTypeEnabled

    private fun scheduleWarningReset() {
        warningResetJob?.cancel()
        warningResetJob = scope.launchDelayed(SelectionWarningDuration) {
            showSelectionWarning = false
        }
    }

    fun toggleSelectionOrShowWarning(fileType: FileType) {
        if (cannotDeselect(fileType)) {
            showSelectionWarning = true
            scheduleWarningReset()
        } else {
            toggleSelection(fileType)
        }
    }
}

@Composable
fun rememberFileTypeSelectionState(
    navigatorConfig: NavigatorConfig,
    toggleSelection: (FileType) -> Unit,
    deleteCustomFileType: (CustomFileType) -> Unit
): FileTypeSelectionState {
    val scope = rememberCoroutineScope()

    val selectionMap = remember(navigatorConfig) {
        navigatorConfig
            .fileTypeConfigMap
            .mapValues { it.value.enabled }
            .toImmutableMap()
    }

    return remember(scope, selectionMap, toggleSelection, deleteCustomFileType) {
        FileTypeSelectionState(
            selectionMap = selectionMap,
            toggleSelection = toggleSelection,
            deleteCustomFileType = deleteCustomFileType,
            scope = scope
        )
    }
}
