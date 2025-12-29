package com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.w2sv.filenavigator.ui.screen.navigatorsettings.NavigatorSettingsScreenViewModel
import com.w2sv.filenavigator.ui.util.snackbar.ScopedSnackbarController
import com.w2sv.filenavigator.ui.util.snackbar.rememberScopedSnackbarController
import kotlinx.coroutines.flow.update

@Composable
fun rememberNavigatorConfigActions(
    navigatorVM: NavigatorSettingsScreenViewModel = hiltViewModel(),
    scopedSnackbarController: ScopedSnackbarController = rememberScopedSnackbarController()
): NavigatorConfigActions =
    remember(navigatorVM.reversibleConfig, scopedSnackbarController) {
        NavigatorConfigActionsImpl(
            config = { navigatorVM.reversibleConfig.value },
            updateConfig = navigatorVM.reversibleConfig::update,
            scopedSnackbarController = scopedSnackbarController
        )
    }
