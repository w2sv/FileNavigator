package com.w2sv.filenavigator.ui.state

import androidx.compose.runtime.Stable
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.viewmodel.MakeSnackbarVisuals
import com.w2sv.kotlinutils.coroutines.flow.emit
import com.w2sv.reversiblestate.ReversibleState
import com.w2sv.reversiblestate.ReversibleStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import slimber.log.i

@Stable
class ReversibleNavigatorConfig(
    private val scope: CoroutineScope,
    reversibleStateFlow: ReversibleStateFlow<NavigatorConfig>,
    private val makeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit
) : ReversibleState by reversibleStateFlow,
    MutableStateFlow<NavigatorConfig> by reversibleStateFlow {

    constructor(
        scope: CoroutineScope,
        navigatorConfigDataSource: NavigatorConfigDataSource,
        makeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit,
        onStateSynced: () -> Unit
    ) : this(
        scope = scope,
        reversibleStateFlow = ReversibleStateFlow(
            scope = scope,
            appliedStateFlow = navigatorConfigDataSource.navigatorConfig.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = NavigatorConfig.default
            ),
            syncState = {
                navigatorConfigDataSource.saveNavigatorConfig(it)
                onStateSynced()
            }
        ),
        makeSnackbarVisuals = makeSnackbarVisuals
    )

    fun onFileTypeCheckedChange(fileType: FileType, checkedNew: Boolean) {
        updateOrEmitSnackbar(
            checkedNew = checkedNew,
            checkedCount = value.enabledFileTypes.size,
            update = {
                update { config ->
                    config.updateFileTypeConfig(fileType) { it.copy(enabled = checkedNew) }
                }
            },
            makeSnackbarVisuals = {
                AppSnackbarVisuals(
                    message = it.getString(
                        R.string.leave_at_least_one_file_type_enabled
                    ),
                    kind = SnackbarKind.Error
                )
            }
        )
    }

    fun onFileSourceCheckedChange(
        fileType: FileType,
        sourceType: SourceType,
        checkedNew: Boolean
    ) {
        updateOrEmitSnackbar(
            checkedNew = checkedNew,
            checkedCount = value.enabledSourceTypesCount(fileType),
            update = {
                update { config ->
                    config.updateSourceConfig(
                        fileType = fileType,
                        sourceType = sourceType
                    ) {
                        it.copy(enabled = checkedNew)
                    }
                }
            },
            makeSnackbarVisuals = {
                AppSnackbarVisuals(
                    message = it.getString(R.string.leave_at_least_one_file_source_selected_or_disable_the_entire_file_type),
                    kind = SnackbarKind.Error
                )
            }
        )
    }

    val selectFileType: SharedFlow<FileType> get() = _selectFileType.asSharedFlow()
    private val _selectFileType = MutableSharedFlow<FileType>()

    fun createCustomFileType(type: CustomFileType) {
        update { it.addCustomFileType(type) }
        _selectFileType.emit(type, scope)
        i { "Emitted $type on selectFileType" }
    }

    fun editCustomFileType(editedType: CustomFileType) {
        update { it.editCustomFileType(editedType) }
    }

    fun deleteCustomFileType(type: CustomFileType) {
        update { it.deleteCustomFileType(type) }
    }

    private inline fun updateOrEmitSnackbar(
        checkedNew: Boolean,
        checkedCount: Int,
        update: () -> Unit,
        crossinline makeSnackbarVisuals: MakeSnackbarVisuals
    ) {
        if (!checkedNew && checkedCount <= 1) {
            makeSnackbarVisuals { makeSnackbarVisuals(it) }
        } else {
            update()
        }
    }
}
