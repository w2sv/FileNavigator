package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.filenavigator.ui.designsystem.AppCard
import com.w2sv.filenavigator.ui.designsystem.ItemLayout
import com.w2sv.filenavigator.ui.designsystem.SwitchItemLayout
import com.w2sv.filenavigator.ui.modelext.useDarkTheme
import com.w2sv.filenavigator.ui.screen.appsettings.model.AppPreferences
import com.w2sv.modules.common.R

@Composable
fun AppearanceSettingsCard(appPreferences: AppPreferences, modifier: Modifier = Modifier) {
    AppCard(
        title = stringResource(id = R.string.appearance),
        modifier = modifier
    ) {
        Column(verticalArrangement = AppSettingsScreenDimens.cardActionsVerticalArrangement, modifier = Modifier.animateContentSize()) {
            ThemeRow(appPreferences = appPreferences)
            DynamicColorsRow(appPreferences = appPreferences)
            // Leave at the end for Modifier.animateContentSize() to animate it in and out.
            AmoledBlackRow(appPreferences = appPreferences)
        }
    }
}

@Composable
private fun ThemeRow(appPreferences: AppPreferences) {
    ItemLayout(
        icon = { SettingsItemIcon(res = R.drawable.ic_nightlight_24) },
        labelRes = R.string.theme
    ) {
        ThemeSelectionRow(
            selected = appPreferences.theme,
            onSelected = appPreferences.setTheme,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        )
    }
}

@Composable
private fun DynamicColorsRow(appPreferences: AppPreferences) {
    if (dynamicColorsSupported) {
        SwitchItemLayout(
            icon = { SettingsItemIcon(res = R.drawable.ic_palette_24) },
            labelRes = R.string.dynamic_colors,
            checked = appPreferences.useDynamicColors,
            onCheckedChange = appPreferences.setUseDynamicColors,
            explanation = stringResource(id = R.string.use_colors_derived_from_your_wallpaper)
        )
    }
}

@Composable
private fun AmoledBlackRow(appPreferences: AppPreferences) {
    if (useDarkTheme(appPreferences.theme)) {
        SwitchItemLayout(
            icon = { SettingsItemIcon(res = R.drawable.ic_contrast_24) },
            labelRes = R.string.amoled_black,
            checked = appPreferences.useAmoledBlackTheme,
            onCheckedChange = appPreferences.setUseAmoledBlackTheme,
            explanation = stringResource(R.string.amoled_black_explanation)
        )
    }
}
