package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.androidutils.content.openUrl
import com.w2sv.common.AppUrl
import com.w2sv.domain.model.Theme
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.ui.designsystem.Icon
import com.w2sv.filenavigator.ui.designsystem.PaddingDefaults
import com.w2sv.filenavigator.ui.screen.appsettings.model.AppPreferences
import com.w2sv.filenavigator.ui.screen.appsettings.model.settingsActionGroups
import com.w2sv.filenavigator.ui.util.PreviewOf
import com.w2sv.filenavigator.ui.util.ScreenPreviews
import com.w2sv.modules.common.R
import java.time.LocalDate

object AppSettingsScreenDimens {
    val verticalArrangement = Arrangement.spacedBy(16.dp)

    val contentPadding
        @Composable
        @ReadOnlyComposable
        get() = PaddingValues(
            horizontal = PaddingDefaults.horizontal,
            vertical = 18.dp
        )

    val cardActionsVerticalArrangement = Arrangement.spacedBy(20.dp)
}

@Composable
fun AppSettingsScreenRoute(viewModel: AppSettingsViewModel = hiltViewModel()) {
    val showStorageVolumeNames by viewModel.showStorageVolumeNames.collectAsStateWithLifecycle()
    val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()

    AppSettingsScreen(
        appPreferences = AppPreferences(
            showStorageVolumeNames = showStorageVolumeNames,
            setShowStorageVolumeNames = viewModel::saveShowStorageVolumeNames,
            theme = themeSettings.theme,
            setTheme = viewModel::saveTheme,
            useAmoledBlackTheme = themeSettings.useAmoledBlackTheme,
            setUseAmoledBlackTheme = viewModel::saveUseAmoledBlackTheme,
            useDynamicColors = themeSettings.useDynamicColors,
            setUseDynamicColors = viewModel::saveUseDynamicColors
        )
    )
}

@Composable
fun AppSettingsScreen(appPreferences: AppPreferences) {
    val context = LocalContext.current
    val actionGroups = remember { settingsActionGroups() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = AppSettingsScreenDimens.verticalArrangement,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = AppSettingsScreenDimens.contentPadding
    ) {
        item { GeneralSettingsCard(appPreferences = appPreferences) }
        item { AppearanceSettingsCard(appPreferences = appPreferences) }
        items(actionGroups, key = { it.titleRes }) {
            SettingsActionGroupCard(
                group = it,
                context = context
            )
        }
        item {
            Text(
                text = stringResource(id = R.string.version, BuildConfig.VERSION_NAME),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { context.openUrl(AppUrl.RELEASES) }
            )
            Text(text = remember { "© 2023 - ${LocalDate.now().year} | w2sv" })
        }
    }
}

@ScreenPreviews
@Composable
private fun Prev() {
    PreviewOf {
        AppSettingsScreen(
            appPreferences = AppPreferences(
                showStorageVolumeNames = true,
                setShowStorageVolumeNames = {},
                theme = Theme.Default,
                setTheme = {},
                useAmoledBlackTheme = false,
                setUseAmoledBlackTheme = {},
                useDynamicColors = true,
                setUseDynamicColors = {}
            )
        )
    }
}

@Composable
fun SettingsItemIcon(@DrawableRes res: Int, modifier: Modifier = Modifier) {
    Icon(
        res = res,
        tint = colorScheme.primary,
        modifier = modifier.size(28.dp)
    )
}
