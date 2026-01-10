package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.core.common.R
import com.w2sv.domain.model.Theme
import com.w2sv.filenavigator.ui.LocalNavigator
import com.w2sv.filenavigator.ui.designsystem.AppCardDefaults
import com.w2sv.filenavigator.ui.designsystem.BackArrowTopAppBar
import com.w2sv.filenavigator.ui.designsystem.IconSize
import com.w2sv.filenavigator.ui.designsystem.ItemRow
import com.w2sv.filenavigator.ui.designsystem.ItemRowIcon
import com.w2sv.filenavigator.ui.designsystem.PaddingDefaults
import com.w2sv.filenavigator.ui.designsystem.SwitchItemRow
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.screen.home.drawer.ThemeSelectionRow
import com.w2sv.filenavigator.ui.util.PreviewOf
import com.w2sv.filenavigator.ui.util.ScreenPreviews
import com.w2sv.filenavigator.ui.util.useDarkTheme

@Composable
fun AppSettingsScreen(navigator: Navigator = LocalNavigator.current, appPreferences: AppPreferences = rememberAppPreferences()) {
    Scaffold(
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(id = R.string.app_settings),
                onBack = { navigator.popBackStack() },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { innerPadding ->
        SettingsCardColumn(
            appPreferences = appPreferences,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PaddingDefaults.horizontal)
        )
    }
}

@ScreenPreviews
@Composable
private fun Prev() {
    PreviewOf {
        AppSettingsScreen(
            appPreferences = AppPreferences(
                showStorageVolumeNames = { true },
                setShowStorageVolumeNames = {},
                theme = { Theme.Default },
                setTheme = {},
                useAmoledBlackTheme = { false },
                setUseAmoledBlackTheme = {},
                useDynamicColors = { true },
                setUseDynamicColors = {}
            )
        )
    }
}

@Composable
private fun SettingsCardColumn(appPreferences: AppPreferences, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsCard(title = stringResource(R.string.general)) {
            SwitchItemRow(
                icon = { AppSettingsItemRowIcon(res = R.drawable.ic_storage_24) },
                labelRes = R.string.show_storage_volume_names,
                checked = appPreferences.showStorageVolumeNames(),
                onCheckedChange = appPreferences.setShowStorageVolumeNames,
                explanation = rememberStyledTextResource(R.string.show_storage_volume_names_explanation)
            )
        }
        SettingsCard(title = stringResource(id = R.string.appearance), modifier = Modifier.padding(bottom = 24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(22.dp), modifier = Modifier.animateContentSize()) {
                ItemRow(
                    icon = { AppSettingsItemRowIcon(res = R.drawable.ic_nightlight_24) },
                    labelRes = R.string.theme
                ) {
                    ThemeSelectionRow(
                        selected = appPreferences.theme(),
                        onSelected = appPreferences.setTheme,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    )
                }
                if (dynamicColorsSupported) {
                    SwitchItemRow(
                        icon = { AppSettingsItemRowIcon(res = R.drawable.ic_palette_24) },
                        labelRes = R.string.dynamic_colors,
                        checked = appPreferences.useDynamicColors(),
                        onCheckedChange = appPreferences.setUseDynamicColors,
                        explanation = stringResource(id = R.string.use_colors_derived_from_your_wallpaper)
                    )
                }
                // Leave at the end for Modifier.animateContentSize() to animate it in and out
                if (useDarkTheme(appPreferences.theme())) {
                    SwitchItemRow(
                        icon = { AppSettingsItemRowIcon(res = R.drawable.ic_contrast_24) },
                        labelRes = R.string.amoled_black,
                        checked = appPreferences.useAmoledBlackTheme(),
                        onCheckedChange = appPreferences.setUseAmoledBlackTheme,
                        explanation = stringResource(R.string.amoled_black_explanation)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSettingsItemRowIcon(@DrawableRes res: Int) {
    ItemRowIcon(
        res = res,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(IconSize.Big)
    )
}

@Composable
private fun SettingsCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = modifier,
        elevation = AppCardDefaults.elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}
