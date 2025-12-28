package com.w2sv.filenavigator.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.ui.navigation.Screen
import com.w2sv.filenavigator.ui.sharedstate.AppPermissions
import com.w2sv.filenavigator.ui.sharedstate.ThemeSettings
import com.w2sv.filenavigator.ui.util.LifecycleLoggingViewModel
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.coroutines.flow.combineStates
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository, @ApplicationContext context: Context) :
    LifecycleLoggingViewModel() {

    val permissions = AppPermissions(preferencesRepository, viewModelScope, context)

    @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
    val startScreen: Screen by threadUnsafeLazy {
        when {
            BuildConfig.DEBUG && BuildConfig.START_SCREEN == "NavigatorSettings" -> Screen.NavigatorSettings
            permissions.anyMissing.value -> Screen.RequiredPermissions
            else -> Screen.Home
        }
    }

    init {
        // Stop FileNavigator when any permission missing
        permissions.anyMissing.filter { it }.collectOn(viewModelScope) {
            FileNavigator.stop(context)
        }
    }

    // ==============
    // Theme
    // ==============

    val themeSettings = preferencesRepository.themeSettings(viewModelScope)

    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.theme.save(theme)
        }
    }

    fun saveUseAmoledBlackTheme(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useAmoledBlackTheme.save(value)
        }
    }

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

private fun PreferencesRepository.themeSettings(scope: CoroutineScope): StateFlow<ThemeSettings> =
    combineStates(
        theme.stateIn(scope, SharingStarted.WhileSubscribed()),
        useAmoledBlackTheme.stateIn(scope, SharingStarted.WhileSubscribed()),
        useDynamicColors.stateIn(scope, SharingStarted.WhileSubscribed())
    ) { theme, useAmoledBlackTheme, useDynamicColors ->
        ThemeSettings(
            theme = theme,
            useAmoledBlackTheme = useAmoledBlackTheme,
            useDynamicColors = useDynamicColors
        )
    }
