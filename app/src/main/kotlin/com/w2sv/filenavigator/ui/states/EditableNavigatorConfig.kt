package com.w2sv.filenavigator.ui.states

import androidx.compose.runtime.Stable
import com.w2sv.androidutils.coroutines.mapState
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Stable
class EditableNavigatorConfig(
    scope: CoroutineScope,
    navigatorConfigDataSource: NavigatorConfigDataSource,
    private val emitMakeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit,
    onStateSynced: () -> Unit,
) {
    val editable = ReversibleStateFlow(
        scope = scope,
        appliedState = navigatorConfigDataSource.navigatorConfig.stateIn(
            scope,
            SharingStarted.Eagerly,
            NavigatorConfig.default
        ),
        syncState = {
            navigatorConfigDataSource.saveNavigatorConfig(it)
            onStateSynced()
        },
    )

    val enabledFileTypes =
        editable.mapState { config -> config.fileTypeConfigMap.run { keys.filter { getValue(it).enabled } } }

    val disabledFileTypes = enabledFileTypes.mapState {
        (FileType.values.toSet() - it.toSet())
            .sortedBy { fileType ->
                FileType.values.indexOf(fileType)
            }
    }

    fun onFileTypeCheckedChange(
        fileType: FileType,
        checkedNew: Boolean
    ) {
        updateOrEmitSnackbar(
            checkedNew = checkedNew,
            checkedCount = enabledFileTypes.value.size,
            update = {
                editable.update { config ->
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

    fun onMediaFileSourceCheckedChange(
        fileType: FileType,
        sourceType: SourceType,
        checkedNew: Boolean
    ) {
        updateOrEmitSnackbar(
            checkedNew = checkedNew,
            checkedCount = editable.value.fileTypeConfigMap.getValue(fileType).sourceTypeToConfig.values.count { it.enabled },
            update = {
                editable.update { config ->
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

//private fun <T> SnapshotStateList<T>.updateFrom(other: List<T>) {  // TODO: export to composed
//    other.forEachIndexed { index, venue ->
//        try {
//            if (venue != get(index)) {
//                this[index] = venue
//            }
//        } catch (e: IndexOutOfBoundsException) {
//            add(venue)
//        }
//    }
//    if (size > other.size) {
//        removeRange(other.size, size)
//    }
//}
//
//private data class ReversibleValue<T>(var value: T) {
//    var previous: T = value
//        private set
//
//    fun sync() {
//        previous = value
//    }
//
//    fun reset() {
//        value = previous
//    }
//}