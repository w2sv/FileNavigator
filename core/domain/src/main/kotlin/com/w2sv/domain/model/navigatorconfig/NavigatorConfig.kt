package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.defaultConfig
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.map
import com.w2sv.kotlinutils.update

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

    val fileTypes by lazy {
        buildSet {
            addAll(enabledFileTypes)
            addAll(disabledFileTypes)
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

    fun addCustomFileType(type: CustomFileType): NavigatorConfig =
        copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                put(type, type.defaultConfig(enabled = false))
            }
        )

    fun editCustomFileType(editedType: CustomFileType): NavigatorConfig {
        val preEditType = fileTypeConfigMap.keys.first { it.ordinal == editedType.ordinal }
        return copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                val fileTypeConfig = requireNotNull(remove(preEditType))
                put(editedType, fileTypeConfig)
            }
        )
    }

    fun deleteCustomFileType(type: CustomFileType): NavigatorConfig =
        copy(fileTypeConfigMap = fileTypeConfigMap.copy { remove(type) })

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
                fileTypeConfigMap = PresetFileType.values.associateWith { fileType -> fileType.defaultConfig() },
                showBatchMoveNotification = true,
                disableOnLowBattery = false,
                startOnBoot = false
            )
        }
    }
}
