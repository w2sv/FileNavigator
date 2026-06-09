package com.w2sv.filenavigator.ui.screen.appsettings.model

import androidx.compose.runtime.Stable
import com.w2sv.domain.model.Theme

@Stable
data class AppPreferences(
    val showStorageVolumeNames: Boolean,
    val setShowStorageVolumeNames: (Boolean) -> Unit,
    val theme: Theme,
    val setTheme: (Theme) -> Unit,
    val useAmoledBlackTheme: Boolean,
    val setUseAmoledBlackTheme: (Boolean) -> Unit,
    val useDynamicColors: Boolean,
    val setUseDynamicColors: (Boolean) -> Unit
)
