package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType

data class NavigatorConfig(
    val fileTypeConfigMap: Map<FileType, FileTypeConfig>,
    val showBatchMoveNotification: Boolean,
    val disableOnLowBattery: Boolean,
    val startOnBoot: Boolean,
) {
    val enabledFileTypes: List<FileType> by lazy {
        fileTypeConfigMap.run { keys.filter { getValue(it).enabled } }
    }

    val disabledFileTypes: List<FileType> by lazy {
        (fileTypeConfigMap.keys - enabledFileTypes.toSet())
            .sortedBy { fileType ->
                fileType.ordinal
            }
    }

    // ================
    // Access Helpers
    // ================

    fun enabledSourceTypesCount(fileType: FileType): Int =
        fileTypeConfig(fileType).sourceTypeConfigMap.values.count { it.enabled }

    fun fileTypeConfig(fileType: FileType): FileTypeConfig =
        fileTypeConfigMap.getValue(fileType)

    fun sourceConfig(fileType: FileType, sourceType: SourceType): SourceConfig =
        fileTypeConfig(fileType).sourceTypeConfigMap.getValue(sourceType)

    // ================
    // Copying
    // ================

    fun copyWithAlteredFileConfig(
        fileType: FileType,
        alterFileConfig: (FileTypeConfig) -> FileTypeConfig
    ): NavigatorConfig =
        copy(
            fileTypeConfigMap = fileTypeConfigMap.toMutableMap()
                .apply { put(fileType, alterFileConfig(getValue(fileType))) }
        )

    fun copyWithAlteredSourceConfig(
        fileType: FileType,
        sourceType: SourceType,
        alterSourceConfig: (SourceConfig) -> SourceConfig
    ): NavigatorConfig =
        copyWithAlteredFileConfig(
            fileType = fileType,
        ) {
            it.copy(
                sourceTypeConfigMap = it.sourceTypeConfigMap.toMutableMap()
                    .apply { put(sourceType, alterSourceConfig(getValue(sourceType))) })
        }

    fun copyWithAlteredSourceAutoMoveConfigs(
        fileType: FileType,
        autoMoveConfig: AutoMoveConfig
    ): NavigatorConfig =
        copyWithAlteredFileConfig(
            fileType = fileType,
        ) {
            @Suppress("SimplifyNestedEachInScopeFunction")
            it.copy(
                sourceTypeConfigMap = it.sourceTypeConfigMap.toMutableMap()
                    .apply {
                        forEach { (sourceType, sourceConfig) ->
                            this[sourceType] = sourceConfig.copy(autoMoveConfig = autoMoveConfig)
                        }
                    }
            )
        }

    fun copyWithAlteredSourceAutoMoveConfig(
        fileType: FileType,
        sourceType: SourceType,
        alterSourceAutoMoveConfig: (AutoMoveConfig) -> AutoMoveConfig
    ) =
        copyWithAlteredSourceConfig(fileType, sourceType) {
            it.copy(autoMoveConfig = alterSourceAutoMoveConfig(it.autoMoveConfig))
        }

    companion object {
        val default by lazy {
            NavigatorConfig(
                fileTypeConfigMap = FileType.values.associateWith { defaultFileTypeConfig(it) },
                showBatchMoveNotification = true,
                disableOnLowBattery = false,
                startOnBoot = false,
            )
        }
    }
}

private fun defaultFileTypeConfig(fileType: FileType): FileTypeConfig =
    FileTypeConfig(
        enabled = true,
        sourceTypeConfigMap = fileType.sourceTypes.associateWith { SourceConfig() },
    )