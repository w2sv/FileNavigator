package com.w2sv.filenavigator.ui.screen.navigatorsettings

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.ui.state.ReversibleNavigatorConfig
import com.w2sv.filenavigator.ui.util.LifecycleLoggingViewModel
import com.w2sv.filenavigator.ui.util.MakeSnackbarVisualsEmitter
import com.w2sv.filenavigator.ui.util.MakeSnackbarVisualsEmitterImpl
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.system_broadcastreceiver.PowerSaveModeChangedReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class NavigatorSettingsScreenViewModel @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    val navigatorIsRunning: FileNavigator.IsRunning,
    @ApplicationContext context: Context
) : LifecycleLoggingViewModel(),
    MakeSnackbarVisualsEmitter by MakeSnackbarVisualsEmitterImpl() {

    init {
        // Launch registering/unregistering of PowerSaveModeChangedReceiver on change of navigatorConfig.disableOnLowBattery
        val receiver by lazy { PowerSaveModeChangedReceiver() }
        navigatorConfigDataSource.navigatorConfig
            .map { it.disableOnLowBattery }
            .drop(1)
            .collectOn(viewModelScope, Dispatchers.Default) { disableOnLowBattery ->
                receiver.setRegistered(register = disableOnLowBattery, context)
            }
    }

    val reversibleConfig = ReversibleNavigatorConfig(
        scope = viewModelScope,
        navigatorConfigDataSource = navigatorConfigDataSource,
        makeSnackbarVisuals = {
            viewModelScope.launch {
                emitMakeSnackbarVisuals(it)
            }
        },
        onStateSynced = {
            if (navigatorIsRunning.value) {
                FileNavigator.reregisterFileObservers(context)
            }
        }
    )

    fun launchConfigSync(): Job =
        viewModelScope.launch { reversibleConfig.sync() }
}
