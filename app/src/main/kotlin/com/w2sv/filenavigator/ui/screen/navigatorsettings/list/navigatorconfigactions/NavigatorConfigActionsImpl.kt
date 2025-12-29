package com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions

import androidx.compose.runtime.Stable
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarAction
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.util.snackbar.ScopedSnackbarController

@Stable
class NavigatorConfigActionsImpl(
    private val config: () -> NavigatorConfig,
    private val updateConfig: ((NavigatorConfig) -> NavigatorConfig) -> Unit,
    private val scopedSnackbarController: ScopedSnackbarController
) : NavigatorConfigActions {

    override fun setSourceTypeEnablement(
        fileType: FileType,
        sourceType: SourceType,
        enabled: Boolean
    ) {
        val config = config()
        if (!enabled && config.enabledSourceTypesCount(fileType) <= 1) {
            scopedSnackbarController.showReplacing {
                AppSnackbarVisuals(
                    message = getString(R.string.source_unselection_warning),
                    kind = SnackbarKind.Error,
                    action = if (config.enabledFileTypes.size > 1) {
                        SnackbarAction(
                            label = getString(R.string.disable),
                            callback = { update { it.toggleFileTypeEnablement(fileType) } }
                        )
                    } else {
                        null
                    }
                )
            }
        } else {
            update { it.updateSourceTypeEnablement(fileType, sourceType, enabled) }
        }
    }

    override fun setAutoMoveConfig(
        fileType: FileType,
        sourceType: SourceType,
        config: AutoMoveConfig
    ) {
        update { it.updateAutoMoveConfig(fileType, sourceType) { config } }
    }

    override fun setAutoMoveConfigs(fileType: FileType, config: AutoMoveConfig) {
        update {
            it.updateAutoMoveConfigs(
                fileType = fileType,
                autoMoveConfig = config
            )
        }
    }

    override fun update(function: (NavigatorConfig) -> NavigatorConfig) {
        updateConfig(function)
    }
}
