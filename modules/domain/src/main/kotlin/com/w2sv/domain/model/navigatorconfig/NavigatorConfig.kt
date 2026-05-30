package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.FileTypeId
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.map
import com.w2sv.kotlinutils.update

typealias FileTypeConfigMap = Map<FileTypeId, FileTypeConfig>

data class NavigatorConfig(
    val fileTypeConfigMap: FileTypeConfigMap,
    val showBatchMoveNotification: Boolean,
    val disableOnLowBattery: Boolean,
    val startOnBoot: Boolean
) {
    val enabledFileTypes: Set<FileType> by lazy {
        fileTypeConfigMap.values
            .filter { it.enabled }
            .mapTo(mutableSetOf()) { it.fileType }
    }

    val sortedEnabledFileTypes: List<FileType> by lazy {
        enabledFileTypes.sortedByOrdinal()
    }

    val disabledFileTypes: Set<FileType> by lazy {
        fileTypeConfigMap.values
            .filterNot { it.enabled }
            .mapTo(mutableSetOf()) { it.fileType }
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
        fileTypeConfigMap.getValue(fileType.id)

    fun sourceConfig(fileType: FileType, sourceType: SourceType): SourceConfig =
        fileTypeConfig(fileType).sourceTypeConfigMap.getValue(sourceType)

    fun autoMoveConfig(fileType: FileType, sourceType: SourceType): AutoMoveConfig =
        sourceConfig(fileType, sourceType).autoMoveConfig

    fun quickMoveDestinations(fileType: FileType, sourceType: SourceType): List<LocalDestinationApi> =
        sourceConfig(fileType, sourceType).quickMoveDestinations

    // ================
    // Copying with modifications
    // ================

    fun toggleFileTypeEnablement(fileType: FileType): NavigatorConfig =
        copy(fileTypeConfigMap = fileTypeConfigMap.copy { update(fileType.id) { it.copy(enabled = !it.enabled) } })

    /**
     * Adds [type] to the configuration with a default [FileTypeConfig], with [FileTypeConfig.enabled] set to [enabled].
     * @see com.w2sv.domain.model.filetype.defaultConfig
     */
    fun addCustomFileType(type: FileType.Custom, enabled: Boolean = false): NavigatorConfig =
        copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                put(type.id, type.defaultConfig(enabled = enabled))
            }
        )

    fun editFileType(current: FileType, mutate: (FileType) -> FileType): NavigatorConfig =
        copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                val fileTypeConfig = getValue(current.id)
                val edited = mutate(current)
                remove(current.id)
                put(edited.id, fileTypeConfig.copy(fileType = edited))
            }
        )

    fun deleteCustomFileType(type: FileType.Custom): NavigatorConfig =
        copy(fileTypeConfigMap = fileTypeConfigMap.copy { remove(type.id) })

    fun updateFileTypeConfig(fileType: FileType, update: (FileTypeConfig) -> FileTypeConfig): NavigatorConfig =
        copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                this.update(fileType.id, update)
            }
        )

    fun updateSourceConfig(fileType: FileType, sourceType: SourceType, update: (SourceConfig) -> SourceConfig): NavigatorConfig =
        updateFileTypeConfig(fileType = fileType) {
            it.copy(
                sourceTypeConfigMap = it.sourceTypeConfigMap.copy {
                    this.update(sourceType, update)
                }
            )
        }

    fun updateSourceTypeEnablement(fileType: FileType, sourceType: SourceType, enabled: Boolean): NavigatorConfig =
        updateSourceConfig(
            fileType = fileType,
            sourceType = sourceType
        ) {
            it.copy(enabled = enabled)
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

    fun updateAutoMoveConfig(fileType: FileType, sourceType: SourceType, modifyAutoMoveConfig: (AutoMoveConfig) -> AutoMoveConfig) =
        updateSourceConfig(fileType, sourceType) {
            it.copy(autoMoveConfig = modifyAutoMoveConfig(it.autoMoveConfig))
        }

    companion object {
        val default by lazy {
            NavigatorConfig(
                fileTypeConfigMap = PresetFileType.entries.associate { presetFileType ->
                    val fileType = presetFileType.toFileType()
                    fileType.id to fileType.defaultConfig()
                },
                showBatchMoveNotification = true,
                disableOnLowBattery = false,
                startOnBoot = false
            )
        }
    }
}

fun Collection<FileType>.sortedByOrdinal(): List<FileType> =
    sortedBy { fileType -> fileType.ordinal }
