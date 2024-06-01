package com.w2sv.filenavigator.ui.sharedviewmodels

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.ui.states.EditableNavigatorConfig
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias MakeSnackbarVisuals = (Context) -> SnackbarVisuals

@HiltViewModel
class NavigatorViewModel @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    fileNavigatorStatus: FileNavigator.Status,
    @ApplicationContext context: Context
) : ViewModel() {

    val makeSnackbarVisuals: SharedFlow<MakeSnackbarVisuals> get() = _makeSnackbarVisuals.asSharedFlow()
    private val _makeSnackbarVisuals = MutableSharedFlow<MakeSnackbarVisuals>()

    val isRunning get() = _isRunning.asStateFlow()
    private val _isRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileNavigator>())

    init {
        viewModelScope.collectFromFlow(fileNavigatorStatus.isRunning) {
            _isRunning.value = it
        }
    }

    val configuration = EditableNavigatorConfig(
        scope = viewModelScope,
        navigatorConfigDataSource = navigatorConfigDataSource,
        emitMakeSnackbarVisuals = {
            viewModelScope.launch {
                _makeSnackbarVisuals.emit(it)
            }
        },
        onStateSynced = {
            if (isRunning.value) {
                FileNavigator.reregisterFileObservers(
                    context
                )
            }
        },
    )
}