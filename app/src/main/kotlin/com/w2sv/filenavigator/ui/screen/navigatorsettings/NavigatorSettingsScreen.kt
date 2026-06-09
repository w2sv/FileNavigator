package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.designsystem.layout.PaddingDefaults
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.NavigatorSettingsList
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.NavigatorConfigActions
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.PreviewNavigatorConfigActions
import com.w2sv.filenavigator.ui.shared.debugging.PreviewOf

@Composable
fun NavigatorSettingsScreen(
    navigatorConfig: NavigatorConfig,
    navigatorConfigActions: NavigatorConfigActions,
    showFileTypesBottomSheet: () -> Unit,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigatorSettingsList(
        config = navigatorConfig,
        actions = navigatorConfigActions,
        showFileTypesBottomSheet = showFileTypesBottomSheet,
        showFileTypeConfigurationDialog = showFileTypeConfigurationDialog,
        modifier = modifier
            .padding(horizontal = PaddingDefaults.horizontal)
            .fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
private fun Prev() {
    PreviewOf {
        NavigatorSettingsScreen(
            navigatorConfig = NavigatorConfig.default,
            navigatorConfigActions = PreviewNavigatorConfigActions,
            showFileTypesBottomSheet = {},
            showFileTypeConfigurationDialog = {}
        )
    }
}

@Preview
@Composable
private fun PrevDark() {
    PreviewOf(useDarkTheme = true) {
        NavigatorSettingsScreen(
            navigatorConfig = NavigatorConfig.default,
            navigatorConfigActions = PreviewNavigatorConfigActions,
            showFileTypesBottomSheet = {},
            showFileTypeConfigurationDialog = {}
        )
    }
}
