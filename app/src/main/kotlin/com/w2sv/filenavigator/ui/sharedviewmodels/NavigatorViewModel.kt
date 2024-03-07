package com.w2sv.filenavigator.ui.sharedviewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.filenavigator.ui.states.NavigatorConfiguration
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NavigatorViewModel @Inject constructor(
    navigatorRepository: NavigatorRepository,
    fileNavigatorStatusChanged: FileNavigator.StatusChanged,
    @ApplicationContext context: Context
) : ViewModel() {

    val isRunning get() = _isRunning.asStateFlow()
    private val _isRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileNavigator>())

    init {
        viewModelScope.collectFromFlow(fileNavigatorStatusChanged.isRunning) {
            _isRunning.value = it
        }
    }

    val configuration = NavigatorConfiguration(
        scope = viewModelScope,
        navigatorRepository = navigatorRepository,
        onStateSynced = {
            if (isRunning.value) {
                FileNavigator.reregisterFileObservers(
                    context
                )
            }
        },
    )
}