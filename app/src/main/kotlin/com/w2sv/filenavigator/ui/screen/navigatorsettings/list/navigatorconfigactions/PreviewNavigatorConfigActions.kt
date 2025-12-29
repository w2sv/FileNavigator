package com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions

import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig

object PreviewNavigatorConfigActions : NavigatorConfigActions {
    override fun setSourceTypeEnablement(
        fileType: FileType,
        sourceType: SourceType,
        enabled: Boolean
    ) =
        Unit

    override fun setAutoMoveConfig(
        fileType: FileType,
        sourceType: SourceType,
        config: AutoMoveConfig
    ) =
        Unit

    override fun setAutoMoveConfigs(fileType: FileType, config: AutoMoveConfig) =
        Unit
    override fun update(function: (NavigatorConfig) -> NavigatorConfig) =
        Unit
}
