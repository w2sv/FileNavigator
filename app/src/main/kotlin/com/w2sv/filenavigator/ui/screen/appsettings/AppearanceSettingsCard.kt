package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.designsystem.component.AppCard
import com.w2sv.designsystem.component.ItemLayout
import com.w2sv.designsystem.component.SwitchItemLayout
import com.w2sv.designsystem.modelext.useDarkTheme
import com.w2sv.domain.model.settings.AppSettings
import com.w2sv.modules.common.R

@Composable
fun AppearanceSettingsCard(appSettings: AppSettings, saveAppSettings: (AppSettings) -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        title = stringResource(id = R.string.appearance),
        modifier = modifier
    ) {
        Column(verticalArrangement = AppSettingsScreenDimens.cardActionsVerticalArrangement, modifier = Modifier.animateContentSize()) {
            ThemeRow(appSettings = appSettings, saveAppSettings = saveAppSettings)
            DynamicColorsRow(appSettings = appSettings, saveAppSettings = saveAppSettings)
            // Leave at the end for Modifier.animateContentSize() to animate it in and out.
            AmoledBlackRow(appSettings = appSettings, saveAppSettings = saveAppSettings)
        }
    }
}

@Composable
private fun ThemeRow(appSettings: AppSettings, saveAppSettings: (AppSettings) -> Unit) {
    ItemLayout(
        icon = { SettingsItemIcon(res = R.drawable.ic_nightlight_24) },
        labelRes = R.string.theme
    ) {
        ThemeSelectionRow(
            selected = appSettings.theme.theme,
            onSelected = {
                saveAppSettings(
                    appSettings.copy(
                        theme = appSettings.theme.copy(theme = it)
                    )
                )
            },
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        )
    }
}

@Composable
private fun DynamicColorsRow(appSettings: AppSettings, saveAppSettings: (AppSettings) -> Unit) {
    if (dynamicColorsSupported) {
        SwitchItemLayout(
            icon = { SettingsItemIcon(res = R.drawable.ic_palette_24) },
            labelRes = R.string.dynamic_colors,
            checked = appSettings.theme.useDynamicColors,
            onCheckedChange = {
                saveAppSettings(
                    appSettings.copy(
                        theme = appSettings.theme.copy(useDynamicColors = it)
                    )
                )
            },
            explanation = stringResource(id = R.string.use_colors_derived_from_your_wallpaper)
        )
    }
}

@Composable
private fun AmoledBlackRow(appSettings: AppSettings, saveAppSettings: (AppSettings) -> Unit) {
    if (useDarkTheme(appSettings.theme.theme)) {
        SwitchItemLayout(
            icon = { SettingsItemIcon(res = R.drawable.ic_contrast_24) },
            labelRes = R.string.amoled_black,
            checked = appSettings.theme.useAmoledBlackTheme,
            onCheckedChange = {
                saveAppSettings(
                    appSettings.copy(
                        theme = appSettings.theme.copy(useAmoledBlackTheme = it)
                    )
                )
            },
            explanation = stringResource(R.string.amoled_black_explanation)
        )
    }
}
