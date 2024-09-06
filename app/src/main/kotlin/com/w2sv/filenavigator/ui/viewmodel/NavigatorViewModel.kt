package com.w2sv.filenavigator.ui.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.ui.state.ReversibleNavigatorConfig
import com.w2sv.kotlinutils.coroutines.collectFromFlow
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.system_action_broadcastreceiver.BootCompletedReceiver
import com.w2sv.navigator.system_action_broadcastreceiver.PowerSaveModeChangedReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

typealias MakeSnackbarVisuals = (Context) -> SnackbarVisuals

@HiltViewModel
class NavigatorViewModel @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    val navigatorIsRunning: FileNavigator.IsRunning,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val makeSnackbarVisuals: SharedFlow<MakeSnackbarVisuals> get() = _makeSnackbarVisuals.asSharedFlow()
    private val _makeSnackbarVisuals = MutableSharedFlow<MakeSnackbarVisuals>()

    private val appliedConfig by navigatorConfigDataSource::navigatorConfig

    private val disabledOnLowBatteryDistinctUntilChanged =
        appliedConfig.map { it.disableOnLowBattery }.distinctUntilChanged()
    private val startOnBootDistinctUntilChanged =
        appliedConfig.map { it.startOnBoot }.distinctUntilChanged()

    private val bootCompletedReceiver by lazy {
        BootCompletedReceiver()
    }
    private val powerSaveModeChangedReceiver by lazy {
        PowerSaveModeChangedReceiver()
    }

    init {
        viewModelScope.collectFromFlow(disabledOnLowBatteryDistinctUntilChanged) {
            i { "Collected disableOnLowBattery=$it" }
            powerSaveModeChangedReceiver.toggle(it, context)
        }
        viewModelScope.collectFromFlow(startOnBootDistinctUntilChanged) {
            i { "Collected startOnBootCompleted=$it" }
            bootCompletedReceiver.toggle(it, context)
        }
    }

    val reversibleConfig = ReversibleNavigatorConfig(
        scope = viewModelScope,
        navigatorConfigDataSource = navigatorConfigDataSource,
        emitMakeSnackbarVisuals = {
            viewModelScope.launch {
                _makeSnackbarVisuals.emit(it)
            }
        },
        onStateSynced = {
            if (navigatorIsRunning.value) {
                FileNavigator.reregisterFileObservers(
                    context
                )
            }
        },
    )
}