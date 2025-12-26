package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.Theme
import com.w2sv.filenavigator.ui.AppViewModel
import com.w2sv.filenavigator.ui.util.activityViewModel

@Stable
data class AppPreferences(
    val showStorageVolumeNames: () -> Boolean,
    val setShowStorageVolumeNames: (Boolean) -> Unit,
    val theme: () -> Theme,
    val setTheme: (Theme) -> Unit,
    val useAmoledBlackTheme: () -> Boolean,
    val setUseAmoledBlackTheme: (Boolean) -> Unit,
    val useDynamicColors: () -> Boolean,
    val setUseDynamicColors: (Boolean) -> Unit
)

@Composable
fun rememberAppPreferences(appVM: AppViewModel = activityViewModel()): AppPreferences {
    val showStorageVolumeNames by appVM.showStorageVolumeNames.collectAsStateWithLifecycle()
    val themeSettings by appVM.themeSettings.collectAsStateWithLifecycle()

    return remember(appVM) {
        AppPreferences(
            showStorageVolumeNames = { showStorageVolumeNames },
            setShowStorageVolumeNames = appVM::saveShowStorageVolumeNames,
            theme = { themeSettings.theme },
            setTheme = appVM::saveTheme,
            useAmoledBlackTheme = { themeSettings.useAmoledBlackTheme },
            setUseAmoledBlackTheme = appVM::saveUseAmoledBlackTheme,
            useDynamicColors = { themeSettings.useDynamicColors },
            setUseDynamicColors = appVM::saveUseDynamicColors
        )
    }
}
