package com.w2sv.domain.model.navigatorconfig

import com.w2sv.common.util.filterKeysByValueToSet
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.ExtensionConfigurableFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.NonMediaFileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.defaultConfig
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.map
import com.w2sv.kotlinutils.update

data class NavigatorConfig(
    val fileTypeConfigMap: Map<FileType, FileTypeConfig>,
    val extensionConfigurableFileTypeToExcludedExtensions: Map<ExtensionConfigurableFileType, Collection<String>>,
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

    val enabledNonMediaFileTypesWithExtensions: List<NonMediaFileType.WithExtensions> by lazy {
        enabledFileTypes.filterIsInstance<NonMediaFileType>().withExtensions()
    }

    val nonMediaFileTypesWithExtensions: List<NonMediaFileType.WithExtensions> by lazy {
        fileTypeConfigMap.keys.filterIsInstance<NonMediaFileType>().withExtensions()
    }

    private fun Collection<NonMediaFileType>.withExtensions(): List<NonMediaFileType.WithExtensions> =
        map { nonMediaFileType ->
            when (nonMediaFileType) {
                is NonMediaFileType.WithExtensions -> nonMediaFileType
                is PresetFileType.NonMedia.ExtensionConfigurable -> PresetFileType.NonMedia.ExtensionConfigured(
                    fileType = nonMediaFileType,
                    excludedExtensions = extensionConfigurableFileTypeToExcludedExtensions.getValue(nonMediaFileType)
                        .toSet() // TODO: evaluate where to convert to Set
                )

                is PresetFileType.NonMedia.ExtensionConfigured -> error("fileTypeConfigMap should not contain file types of type PresetFileType.NonMedia.ExtensionConfigured") // TODO: refactor so that impossible
            }
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
     * @see defaultConfig
     */
    fun addCustomFileType(type: CustomFileType, enabled: Boolean = false): NavigatorConfig =
        copy(
            fileTypeConfigMap = fileTypeConfigMap.copy {
                put(type, type.defaultConfig(enabled = enabled))
            }
        )

    /**
     * Replaces the current pre-edited [CustomFileType] with [editedType] whilst keeping the corresponding [FileTypeConfig].
     * Pre-edited and [editedType] will be matched through their [CustomFileType.ordinal].
     */
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
                fileTypeConfigMap = PresetFileType.values.associateWith { fileType -> fileType.defaultConfig() },
                extensionConfigurableFileTypeToExcludedExtensions = PresetFileType.NonMedia.ExtensionConfigurable.values.associateWith { emptySet() },
                showBatchMoveNotification = true,
                disableOnLowBattery = false,
                startOnBoot = false
            )
        }
    }
}

private fun Collection<FileType>.sortedByOrdinal(): List<FileType> =
    sortedBy { fileType -> fileType.ordinal }
