package com.w2sv.filenavigator.ui.screen.navigatorsettings.list

import androidx.compose.runtime.Stable
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig

@Stable
interface NavigatorConfigActions {
    fun toggleSource(
        fileType: FileType,
        sourceType: SourceType,
        enabled: Boolean
    )

    fun setAutoMoveConfig(
        fileType: FileType,
        sourceType: SourceType,
        config: AutoMoveConfig
    )

    fun setAutoMoveConfigs(fileType: FileType, config: AutoMoveConfig)

    fun update(function: (NavigatorConfig) -> NavigatorConfig)
}
