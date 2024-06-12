package com.w2sv.filenavigator.ui.states

import androidx.compose.runtime.Stable
import com.w2sv.androidutils.ui.reversible_state.ReversibleState
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateFlow
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.sharedviewmodels.MakeSnackbarVisuals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Stable
class ReversibleNavigatorConfig(
    reversibleStateFlow: ReversibleStateFlow<NavigatorConfig>,
    private val emitMakeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit,
    private val cancelSnackbar: () -> Unit,
) : ReversibleState by reversibleStateFlow,
    MutableStateFlow<NavigatorConfig> by reversibleStateFlow {

    constructor(
        scope: CoroutineScope,
        navigatorConfigDataSource: NavigatorConfigDataSource,
        emitMakeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit,
        cancelSnackbar: () -> Unit,
        onStateSynced: () -> Unit
    ) : this(
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
            },
            onStateReset = {
                cancelSnackbar()
            }
        ),
        emitMakeSnackbarVisuals = emitMakeSnackbarVisuals,
        cancelSnackbar = cancelSnackbar
    )

    fun hasChangedWithDelay(scope: CoroutineScope, delayMillis: Long): StateFlow<Boolean> =
        statesDissimilar
            .onEach { delay(delayMillis) }
            .stateIn(scope, SharingStarted.Eagerly, statesDissimilar.value)

    fun updateAndCancelSnackbar(function: (NavigatorConfig) -> NavigatorConfig) {
        update(function)
        cancelSnackbar()
    }

    fun onFileTypeCheckedChange(
        fileType: FileType,
        checkedNew: Boolean
    ) {
        updateOrEmitSnackbar(
            checkedNew = checkedNew,
            checkedCount = value.enabledFileTypes.size,
            update = {
                updateAndCancelSnackbar { config ->
                    config.copyWithAlteredFileConfig(fileType) { it.copy(enabled = checkedNew) }
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
                updateAndCancelSnackbar { config ->
                    config.copyWithAlteredSourceConfig(
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

    private inline fun updateOrEmitSnackbar(
        checkedNew: Boolean,
        checkedCount: Int,
        update: () -> Unit,
        crossinline makeSnackbarVisuals: MakeSnackbarVisuals
    ) {
        if (!checkedNew && checkedCount <= 1) {
            emitMakeSnackbarVisuals { makeSnackbarVisuals(it) }
        } else {
            update()
        }
    }
}