package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.isLandscapeModeActive
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.LocalNavigator
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.BackArrowTopAppBar
import com.w2sv.filenavigator.ui.designsystem.PaddingDefaults
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.NavigatorSettingsList
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.NavigatorConfigActions
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.PreviewNavigatorConfigActions
import com.w2sv.filenavigator.ui.util.PreviewOf
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun NavigatorSettingsScreen(
    navigatorConfig: NavigatorConfig,
    navigatorConfigActions: NavigatorConfigActions,
    configEditState: ConfigEditState,
    showFileTypesBottomSheet: () -> Unit,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    navigator: Navigator = LocalNavigator.current
) {
    Scaffold(
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(id = R.string.navigator_settings),
                onBack = navigator::popBackStack
            )
        },
        floatingActionButton = {
            EditingFabButtonRow(
                configEditState = configEditState,
                modifier = Modifier
                    .padding(
                        top = 8.dp, // Snackbar padding
                        end = if (isLandscapeModeActive) 38.dp else 0.dp
                    )
                    .height(70.dp)
            )
        },
        snackbarHost = { AppSnackbarHost() }
    ) { paddingValues ->
        NavigatorSettingsList(
            config = navigatorConfig,
            actions = navigatorConfigActions,
            showFileTypesBottomSheet = showFileTypesBottomSheet,
            showFileTypeConfigurationDialog = showFileTypeConfigurationDialog,
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = PaddingDefaults.horizontal)
                .fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        NavigatorSettingsScreen(
            navigatorConfig = NavigatorConfig.default,
            navigatorConfigActions = PreviewNavigatorConfigActions,
            configEditState = ConfigEditState(
                hasChanges = { true },
                reset = {},
                apply = {},
                changesHaveBeenApplied = emptyFlow()
            ),
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
            configEditState = ConfigEditState(
                hasChanges = { true },
                reset = {},
                apply = {},
                changesHaveBeenApplied = emptyFlow()
            ),
            showFileTypesBottomSheet = {},
            showFileTypeConfigurationDialog = {}
        )
    }
}
