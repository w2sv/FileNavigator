package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.SourceType

data class FileTypeConfig(
    val enabled: Boolean,
    val sourceTypeToConfig: Map<SourceType, SourceConfig>,
    val autoMoveConfig: AutoMoveConfig
) {
    companion object {
        fun default(sourceTypes: List<SourceType> = emptyList()): FileTypeConfig =
            FileTypeConfig(
                enabled = true,
                sourceTypeToConfig = sourceTypes.associateWith { SourceConfig() },
                autoMoveConfig = AutoMoveConfig()
            )
    }
}