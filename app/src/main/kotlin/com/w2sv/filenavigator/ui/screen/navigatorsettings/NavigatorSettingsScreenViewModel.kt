package com.w2sv.filenavigator.ui.screen.navigatorsettings

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.filenavigator.ui.util.LoggingViewModel
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.di.FileNavigatorIsRunning
import com.w2sv.navigator.system_broadcastreceiver.PowerSaveModeChangedReceiver
import com.w2sv.reversiblestate.ReversibleStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NavigatorSettingsScreenViewModel @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    private val preferencesRepository: PreferencesRepository,
    @FileNavigatorIsRunning val navigatorIsRunning: StateFlow<Boolean>,
    @ApplicationContext context: Context
) : LoggingViewModel() {

    init {
        // Launch registering/unregistering of PowerSaveModeChangedReceiver on change of navigatorConfig.disableOnLowBattery
        val receiver by lazy { PowerSaveModeChangedReceiver() }
        navigatorConfigDataSource.config
            .map { it.disableOnLowBattery }
            .drop(1)
            .collectOn(viewModelScope, Dispatchers.Default) { disableOnLowBattery ->
                receiver.setRegistered(register = disableOnLowBattery, context)
            }
    }

    val configChangesHaveBeenApplied = navigatorConfigDataSource.config.distinctUntilChanged().drop(1).map {}

    val showAutoMoveIntroduction = preferencesRepository.showAutoMoveIntroduction.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    val reversibleConfig = ReversibleStateFlow(
        scope = viewModelScope,
        appliedState = navigatorConfigDataSource.config.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = NavigatorConfig.default
        ),
        commitState = { state ->
            navigatorConfigDataSource.update { state }
            if (navigatorIsRunning.value) {
                FileNavigator.reregisterFileObservers(context)
            }
        }
    )

    fun dismissAutoMoveIntroduction() {
        viewModelScope.launch {
            preferencesRepository.showAutoMoveIntroduction.save(false)
        }
    }
}
