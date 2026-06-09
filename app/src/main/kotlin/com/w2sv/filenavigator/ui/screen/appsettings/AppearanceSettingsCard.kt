package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.settings.Theme
import com.w2sv.domain.model.settings.ThemeSettings
import com.w2sv.filenavigator.ui.designsystem.AppCard
import com.w2sv.filenavigator.ui.designsystem.ItemLayout
import com.w2sv.filenavigator.ui.designsystem.SwitchItemLayout
import com.w2sv.filenavigator.ui.modelext.useDarkTheme
import com.w2sv.modules.common.R

@Composable
fun AppearanceSettingsCard(
    themeSettings: ThemeSettings,
    updateThemeSettings: ((ThemeSettings) -> ThemeSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        title = stringResource(id = R.string.appearance),
        modifier = modifier
    ) {
        Column(verticalArrangement = AppSettingsScreenDimens.cardActionsVerticalArrangement, modifier = Modifier.animateContentSize()) {
            ThemeRow(
                theme = themeSettings.theme,
                onThemeChange = { theme -> updateThemeSettings { it.copy(theme = theme) } }
            )
            DynamicColorsRow(
                useDynamicColors = themeSettings.useDynamicColors,
                onUseDynamicColorsChange = { value -> updateThemeSettings { it.copy(useDynamicColors = value) } }
            )
            // Leave at the end for Modifier.animateContentSize() to animate it in and out.
            AmoledBlackRow(
                theme = themeSettings.theme,
                useAmoledBlackTheme = themeSettings.useAmoledBlackTheme,
                onUseAmoledBlackThemeChange = { value -> updateThemeSettings { it.copy(useAmoledBlackTheme = value) } }
            )
        }
    }
}

@Composable
private fun ThemeRow(theme: Theme, onThemeChange: (Theme) -> Unit) {
    ItemLayout(
        icon = { SettingsItemIcon(res = R.drawable.ic_nightlight_24) },
        labelRes = R.string.theme
    ) {
        ThemeSelectionRow(
            selected = theme,
            onSelected = onThemeChange,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        )
    }
}

@Composable
private fun DynamicColorsRow(useDynamicColors: Boolean, onUseDynamicColorsChange: (Boolean) -> Unit) {
    if (dynamicColorsSupported) {
        SwitchItemLayout(
            icon = { SettingsItemIcon(res = R.drawable.ic_palette_24) },
            labelRes = R.string.dynamic_colors,
            checked = useDynamicColors,
            onCheckedChange = onUseDynamicColorsChange,
            explanation = stringResource(id = R.string.use_colors_derived_from_your_wallpaper)
        )
    }
}

@Composable
private fun AmoledBlackRow(theme: Theme, useAmoledBlackTheme: Boolean, onUseAmoledBlackThemeChange: (Boolean) -> Unit) {
    if (useDarkTheme(theme)) {
        SwitchItemLayout(
            icon = { SettingsItemIcon(res = R.drawable.ic_contrast_24) },
            labelRes = R.string.amoled_black,
            checked = useAmoledBlackTheme,
            onCheckedChange = onUseAmoledBlackThemeChange,
            explanation = stringResource(R.string.amoled_black_explanation)
        )
    }
}
