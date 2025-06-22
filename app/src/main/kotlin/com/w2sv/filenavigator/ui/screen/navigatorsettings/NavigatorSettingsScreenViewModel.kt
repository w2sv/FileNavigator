package com.w2sv.filenavigator.ui.screen.navigatorsettings

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.ui.state.ReversibleNavigatorConfig
import com.w2sv.filenavigator.ui.util.LifecycleLoggingViewModel
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.system_broadcastreceiver.manager.NavigatorConfigControlledSystemBroadcastReceiverManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

typealias MakeSnackbarVisuals = (Context) -> SnackbarVisuals

@HiltViewModel
class NavigatorSettingsScreenViewModel @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    val navigatorIsRunning: FileNavigator.IsRunning,
    navigatorConfigControlledSystemBroadcastReceiverManager: NavigatorConfigControlledSystemBroadcastReceiverManager,
    @ApplicationContext context: Context
) : LifecycleLoggingViewModel() {

    init {
        navigatorConfigControlledSystemBroadcastReceiverManager.toggleReceiversOnStatusChange(
            viewModelScope,
            context
        )
    }

    val makeSnackbarVisuals: SharedFlow<MakeSnackbarVisuals> get() = _makeSnackbarVisuals.asSharedFlow()
    private val _makeSnackbarVisuals = MutableSharedFlow<MakeSnackbarVisuals>()

    val reversibleConfig = ReversibleNavigatorConfig(
        scope = viewModelScope,
        navigatorConfigDataSource = navigatorConfigDataSource,
        makeSnackbarVisuals = {
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
        }
    )

    fun launchConfigSync(): Job =
        viewModelScope.launch { reversibleConfig.sync() }
}
