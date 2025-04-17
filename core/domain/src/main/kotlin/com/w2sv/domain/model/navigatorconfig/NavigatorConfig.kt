package com.w2sv.domain.model.navigatorconfig

import com.w2sv.common.util.filterKeysByValueToSet
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.domain.model.SourceType
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.map
import com.w2sv.kotlinutils.update

typealias FileTypeConfigMap = Map<FileType, FileTypeConfig>

data class NavigatorConfig(
    val fileTypeConfigMap: FileTypeConfigMap,
    val showBatchMoveNotification: Boolean,
    val disableOnLowBattery: Boolean,
    val startOnBoot: Boolean
) {
    val enabledFileTypes: Set<FileType> by lazy {
        fileTypeConfigMap.filterKeysByValueToSet { it.enabled }
    }

    val sortedEnabledFileTypes: List<FileType> by lazy {
        enabledFileTypes.sortedByOrdinal()
    }

    val disabledFileTypes: Set<FileType> by lazy {
        fileTypeConfigMap.keys - enabledFileTypes
    }

    val sortedDisabledFileTypes: List<FileType> by lazy {
        disabledFileTypes.sortedByOrdinal()
    }

    /**
     * @return A [Set] of all [FileType]s included in the config
     */
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
    // Copying with modifications
    // ================

    /**
     * Adds [type] to the configuration with a default [FileTypeConfig], with [FileTypeConfig.enabled] set to [enabled].
     * @see com.w2sv.domain.model.StaticFileType.defaultConfig
     */
    fun addCustomFileType(type: CustomFileType, enabled: Boolean = false): NavigatorConfig =
        copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                put(type, type.defaultConfig(enabled = enabled))
            }
        )

    fun <FT : FileType> editFileType(current: FT, mutate: (FT) -> FT): NavigatorConfig {
        return copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                val fileTypeConfig = requireNotNull(remove(current))
                put(mutate(current), fileTypeConfig)
            }
        )
    }

    fun deleteCustomFileType(type: CustomFileType): NavigatorConfig =
        copy(fileTypeConfigMap = fileTypeConfigMap.copy { remove(type) })

    fun updateFileTypeConfig(fileType: FileType, update: (FileTypeConfig) -> FileTypeConfig): NavigatorConfig =
        copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                this.update(fileType, update)
            }
        )

    fun updateSourceConfig(
        fileType: FileType,
        sourceType: SourceType,
        update: (SourceConfig) -> SourceConfig
    ): NavigatorConfig =
        updateFileTypeConfig(fileType = fileType) {
            it.copy(
                sourceTypeConfigMap = it.sourceTypeConfigMap.copy {
                    this.update(sourceType, update)
                }
            )
        }

    /**
     * Sets all [AutoMoveConfig]s of [fileType]'s [SourceType]'s to [autoMoveConfig].
     */
    fun updateAutoMoveConfigs(fileType: FileType, autoMoveConfig: AutoMoveConfig): NavigatorConfig =
        updateFileTypeConfig(fileType = fileType) {
            it.copy(
                sourceTypeConfigMap = it.sourceTypeConfigMap.map { (sourceType, sourceConfig) ->
                    sourceType to sourceConfig.copy(autoMoveConfig = autoMoveConfig)
                }
            )
        }

    fun updateAutoMoveConfig(
        fileType: FileType,
        sourceType: SourceType,
        modifyAutoMoveConfig: (AutoMoveConfig) -> AutoMoveConfig
    ) = updateSourceConfig(fileType, sourceType) {
        it.copy(autoMoveConfig = modifyAutoMoveConfig(it.autoMoveConfig))
    }

    companion object {
        val default by lazy {
            NavigatorConfig(
                fileTypeConfigMap = PresetFileType.values.associate { presetFileType ->
                    when (presetFileType) {
                        is PresetFileType.ExtensionSet -> presetFileType.toFileType()
                        is PresetFileType.ExtensionConfigurable -> presetFileType.toFileType()
                    } to presetFileType.defaultConfig()
                },
                showBatchMoveNotification = true,
                disableOnLowBattery = false,
                startOnBoot = false
            )
        }
    }
}

private fun Collection<FileType>.sortedByOrdinal(): List<FileType> =
    sortedBy { fileType -> fileType.ordinal }
