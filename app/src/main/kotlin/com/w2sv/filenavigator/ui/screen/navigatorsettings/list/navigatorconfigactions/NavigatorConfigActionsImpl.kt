package com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions

import androidx.compose.runtime.Stable
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.screen.navigatorsettings.ReversibleNavigatorConfig
import kotlinx.coroutines.flow.update

@Stable
class NavigatorConfigActionsImpl(private val reversibleConfig: ReversibleNavigatorConfig) : NavigatorConfigActions {

    override fun toggleSource(
        fileType: FileType,
        sourceType: SourceType,
        enabled: Boolean
    ) {
        reversibleConfig.onFileSourceCheckedChange(
            fileType = fileType,
            sourceType = sourceType,
            checkedNew = enabled
        )
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
        reversibleConfig.update(function)
    }
}
