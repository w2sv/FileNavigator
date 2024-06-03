package com.w2sv.filenavigator.ui.sharedviewmodels

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.ui.states.ReversibleNavigatorConfig
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias MakeSnackbarVisuals = (Context) -> SnackbarVisuals

@HiltViewModel
class NavigatorViewModel @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    val isRunning: FileNavigator.IsRunningStateFlow,
    @ApplicationContext context: Context
) : ViewModel() {

    val makeSnackbarVisuals: SharedFlow<MakeSnackbarVisuals> get() = _makeSnackbarVisuals.asSharedFlow()
    private val _makeSnackbarVisuals = MutableSharedFlow<MakeSnackbarVisuals>()

    val appliedConfig by navigatorConfigDataSource::navigatorConfig

    val reversibleConfig = ReversibleNavigatorConfig(
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