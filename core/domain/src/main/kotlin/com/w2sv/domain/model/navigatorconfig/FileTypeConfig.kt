package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.filetype.SourceType

typealias SourceTypeConfigMap = Map<SourceType, SourceConfig>

data class FileTypeConfig(val enabled: Boolean, val sourceTypeConfigMap: SourceTypeConfigMap)
