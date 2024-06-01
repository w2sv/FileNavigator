package com.w2sv.domain.model.navigatorconfig

data class NavigatorConfig(
    val fileTypeConfigs: List<FileTypeConfig>,
    val disableOnLowBattery: Boolean
)