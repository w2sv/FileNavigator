package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.FileType

data class FileTypeConfig(
    val fileType: FileType,
    val enabled: Boolean,
    val sourceConfigs: List<SourceConfig>,
    val autoMoveConfig: AutoMoveConfig
)