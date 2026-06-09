package com.w2sv.filenavigator.ui.sharedstate

import androidx.compose.runtime.Stable
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.coroutines.flow.combineStates
import com.w2sv.kotlinutils.coroutines.flow.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

@Stable
data class ThemeSettings(val theme: Theme, val useAmoledBlackTheme: Boolean, val useDynamicColors: Boolean)

fun PreferencesRepository.themeSettings(scope: CoroutineScope): StateFlow<ThemeSettings> =
    combineStates(
        theme.stateIn(scope, SharingStarted.WhileSubscribed()),
        useAmoledBlackTheme.stateIn(scope, SharingStarted.WhileSubscribed()),
        useDynamicColors.stateIn(scope, SharingStarted.WhileSubscribed())
            .mapState { it && dynamicColorsSupported } // TODO: move into the repository once PersistedPreference supports map.
    ) { theme, useAmoledBlackTheme, useDynamicColors ->
        ThemeSettings(
            theme = theme,
            useAmoledBlackTheme = useAmoledBlackTheme,
            useDynamicColors = useDynamicColors
        )
    }
