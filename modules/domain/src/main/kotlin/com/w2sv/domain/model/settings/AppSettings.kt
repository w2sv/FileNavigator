package com.w2sv.domain.model.settings

import com.w2sv.androidutils.os.dynamicColorsSupported

data class AppSettings(val showStorageVolumeNames: Boolean, val theme: ThemeSettings) {
    companion object {
        val Default = AppSettings(
            showStorageVolumeNames = true,
            theme = ThemeSettings(
                theme = Theme.Default,
                useAmoledBlackTheme = false,
                useDynamicColors = dynamicColorsSupported
            )
        )
    }
}
