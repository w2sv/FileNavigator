package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.SourceType

data class FileTypeConfig(
    val enabled: Boolean,
    val sourceTypeConfigMap: Map<SourceType, SourceConfig>,
)