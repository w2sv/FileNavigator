package com.w2sv.filenavigator.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.filenavigator.ui.state.AppPermissions
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext context: Context
) :
    ViewModel() {

    val permissions = AppPermissions(preferencesRepository, viewModelScope, context)

    init {
        // Stop FileNavigator when any permission missing
        permissions.anyMissing.collectOn(viewModelScope) {
            if (it) {
                FileNavigator.stop(context)
            }
        }
    }

    // ==============
    // Theme
    // ==============

    val theme = preferencesRepository.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.theme.save(theme)
        }
    }

    val useAmoledBlackTheme = preferencesRepository.useAmoledBlackTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    fun saveUseAmoledBlackTheme(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useAmoledBlackTheme.save(value)
        }
    }

    val useDynamicColors = preferencesRepository.useDynamicColors.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    fun saveUseDynamicColors(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useDynamicColors.save(value)
        }
    }

    // ==============
    // Other
    // ==============

    val showStorageVolumeNames = preferencesRepository.showStorageVolumeNames.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    fun saveShowStorageVolumeNames(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.showStorageVolumeNames.save(value)
        }
    }

    val showAutoMoveIntroduction = preferencesRepository.showAutoMoveIntroduction.stateIn(
        viewModelScope,
        SharingStarted.Eagerly
    )

    fun saveShowAutoMoveIntroduction(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.showAutoMoveIntroduction.save(value)
        }
    }
}
