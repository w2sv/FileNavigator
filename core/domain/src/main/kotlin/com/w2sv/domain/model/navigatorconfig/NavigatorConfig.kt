package com.w2sv.domain.model.navigatorconfig

import com.w2sv.common.util.copy
import com.w2sv.common.util.map
import com.w2sv.common.util.update
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType

data class NavigatorConfig(
    val fileTypeConfigMap: Map<FileType, FileTypeConfig>,
    val showBatchMoveNotification: Boolean,
    val disableOnLowBattery: Boolean,
    val startOnBoot: Boolean
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

    fun copyWithAlteredFileTypeConfig(fileType: FileType, alterFileTypeConfig: (FileTypeConfig) -> FileTypeConfig): NavigatorConfig =
        copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                update(fileType, alterFileTypeConfig)
            }
        )

    fun copyWithAlteredSourceConfig(
        fileType: FileType,
        sourceType: SourceType,
        alterSourceConfig: (SourceConfig) -> SourceConfig
    ): NavigatorConfig =
        copyWithAlteredFileTypeConfig(
            fileType = fileType
        ) {
            it.copy(
                sourceTypeConfigMap = it.sourceTypeConfigMap.copy {
                    update(sourceType, alterSourceConfig)
                }
            )
        }

    fun copyWithAlteredAutoMoveConfigs(fileType: FileType, autoMoveConfig: AutoMoveConfig): NavigatorConfig =
        copyWithAlteredFileTypeConfig(
            fileType = fileType
        ) {
            it.copy(
                sourceTypeConfigMap = it.sourceTypeConfigMap.map { (sourceType, sourceConfig) ->
                    sourceType to sourceConfig.copy(autoMoveConfig = autoMoveConfig)
                }
            )
        }

    fun copyWithAlteredAutoMoveConfig(
        fileType: FileType,
        sourceType: SourceType,
        alterSourceAutoMoveConfig: (AutoMoveConfig) -> AutoMoveConfig
    ) = copyWithAlteredSourceConfig(fileType, sourceType) {
        it.copy(autoMoveConfig = alterSourceAutoMoveConfig(it.autoMoveConfig))
    }

    companion object {
        val default by lazy {
            NavigatorConfig(
                fileTypeConfigMap = FileType.values.associateWith { defaultFileTypeConfig(it) },
                showBatchMoveNotification = true,
                disableOnLowBattery = false,
                startOnBoot = false
            )
        }
    }
}

private fun defaultFileTypeConfig(fileType: FileType): FileTypeConfig =
    FileTypeConfig(
        enabled = true,
        sourceTypeConfigMap = fileType.sourceTypes.associateWith { SourceConfig() }
    )
