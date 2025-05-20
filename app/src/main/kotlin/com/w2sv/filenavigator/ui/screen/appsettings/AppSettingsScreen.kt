package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.composed.rememberStyledTextResource
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.LocalUseDarkTheme
import com.w2sv.filenavigator.ui.designsystem.AppCardDefaults
import com.w2sv.filenavigator.ui.designsystem.BackArrowTopAppBar
import com.w2sv.filenavigator.ui.designsystem.DefaultItemRowIcon
import com.w2sv.filenavigator.ui.designsystem.IconSize
import com.w2sv.filenavigator.ui.designsystem.ItemRow
import com.w2sv.filenavigator.ui.designsystem.NavigationTransitions
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.Spacing
import com.w2sv.filenavigator.ui.designsystem.SwitchItemRow
import com.w2sv.filenavigator.ui.designsystem.drawer.ThemeSelectionRow
import com.w2sv.filenavigator.ui.designsystem.drawer.model.AppPreferences
import com.w2sv.filenavigator.ui.designsystem.drawer.model.rememberAppPreferences

@Destination<RootGraph>(style = NavigationTransitions::class)
@Composable
fun AppSettingsScreen(navigator: DestinationsNavigator, appPreferences: AppPreferences = rememberAppPreferences()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)
    ) {
        BackArrowTopAppBar(
            title = stringResource(id = R.string.app_settings),
            onBack = remember { { navigator.popBackStack() } },
            modifier = Modifier.padding(bottom = 16.dp)
        )
        SettingsCardColumn(
            appPreferences = appPreferences,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Padding.defaultHorizontal)
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
                icon = { ItemRowIcon(res = R.drawable.ic_storage_24) },
                labelRes = R.string.show_storage_volume_names,
                checked = appPreferences.showStorageVolumeNames(),
                onCheckedChange = appPreferences.setShowStorageVolumeNames,
                explanation = rememberStyledTextResource(R.string.show_storage_volume_names_explanation)
            )
        }
        SettingsCard(title = stringResource(id = R.string.appearance)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.VerticalItemRow), modifier = Modifier.animateContentSize()) {
                ItemRow(
                    icon = { ItemRowIcon(res = R.drawable.ic_nightlight_24) },
                    labelRes = R.string.theme,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemeSelectionRow(
                        selected = appPreferences.theme(),
                        onSelected = appPreferences.setTheme,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    )
                }
                if (dynamicColorsSupported) {
                    SwitchItemRow(
                        icon = { ItemRowIcon(res = R.drawable.ic_palette_24) },
                        labelRes = R.string.dynamic_colors,
                        checked = appPreferences.useDynamicColors(),
                        onCheckedChange = appPreferences.setUseDynamicColors,
                        explanation = stringResource(id = R.string.use_colors_derived_from_your_wallpaper)
                    )
                }
                // Leave at the end for Modifier.animateContentSize() to animate it in and out
                if (LocalUseDarkTheme.current) {
                    SwitchItemRow(
                        icon = { ItemRowIcon(res = R.drawable.ic_contrast_24) },
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
private fun ItemRowIcon(@DrawableRes res: Int) {
    DefaultItemRowIcon(
        res = res,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(IconSize.Big)
    )
}

@Composable
private fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
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
