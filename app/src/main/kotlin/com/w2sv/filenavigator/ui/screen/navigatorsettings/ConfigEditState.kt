package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow

@Stable
data class ConfigEditState(
    val hasChanges: () -> Boolean,
    val reset: () -> Unit,
    val apply: () -> Unit,
    val changesHaveBeenApplied: Flow<Unit>
)

@Composable
fun rememberConfigEditState(navigatorVM: NavigatorSettingsScreenViewModel = hiltViewModel()): ConfigEditState {
    val configHasChanges by navigatorVM.reversibleConfig.statesDissimilar.collectAsStateWithLifecycle()
    return remember(navigatorVM) {
        ConfigEditState(
            hasChanges = { configHasChanges },
            reset = navigatorVM.reversibleConfig::reset,
            apply = navigatorVM::launchConfigSync,
            changesHaveBeenApplied = navigatorVM.configChangesHaveBeenApplied
        )
    }
}
