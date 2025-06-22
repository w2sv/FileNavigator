package com.w2sv.filenavigator.ui.screen.navigatorsettings

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.ui.state.ReversibleNavigatorConfig
import com.w2sv.filenavigator.ui.util.LifecycleLoggingViewModel
import com.w2sv.filenavigator.ui.util.MakeSnackbarVisualsEmitter
import com.w2sv.filenavigator.ui.util.MakeSnackbarVisualsEmitterImpl
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.system_broadcastreceiver.manager.NavigatorConfigControlledSystemBroadcastReceiverManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigatorSettingsScreenViewModel @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    val navigatorIsRunning: FileNavigator.IsRunning,
    navigatorConfigControlledSystemBroadcastReceiverManager: NavigatorConfigControlledSystemBroadcastReceiverManager,
    @ApplicationContext context: Context
) : LifecycleLoggingViewModel(),
    MakeSnackbarVisualsEmitter by MakeSnackbarVisualsEmitterImpl() {

    init {
        navigatorConfigControlledSystemBroadcastReceiverManager.toggleReceiversOnStatusChange(
            viewModelScope,
            context
        )
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
                FileNavigator.reregisterFileObservers(
                    context
                )
            }
        }
    )

    fun launchConfigSync(): Job =
        viewModelScope.launch { reversibleConfig.sync() }
}
