package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType

data class NavigatorConfig(
    val fileTypeConfigMap: Map<FileType, FileTypeConfig>,
    val disableOnLowBattery: Boolean
) {
    fun fileTypeConfig(fileType: FileType): FileTypeConfig =
        fileTypeConfigMap.getValue(fileType)

    fun sourceConfig(fileType: FileType, sourceType: SourceType): SourceConfig =
        fileTypeConfig(fileType).sourceTypeToConfig.getValue(sourceType)

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
                sourceTypeToConfig = it.sourceTypeToConfig.toMutableMap()
                    .apply { put(sourceType, alterSourceConfig(getValue(sourceType))) })
        }

    companion object {
        val default by lazy {
            NavigatorConfig(
                fileTypeConfigMap = mapOf(
                    FileType.Image to FileTypeConfig.default(
                        listOf(
                            SourceType.Camera,
                            SourceType.Screenshot,
                            SourceType.OtherApp,
                            SourceType.Download,
                        )
                    ),
                    FileType.Video to FileTypeConfig.default(
                        listOf(
                            SourceType.Camera,
                            SourceType.OtherApp,
                            SourceType.Download,
                        )
                    ),
                    FileType.Audio to FileTypeConfig.default(
                        listOf(
                            SourceType.Recording,
                            SourceType.OtherApp,
                            SourceType.Download,
                        )
                    ),
                    FileType.PDF to FileTypeConfig.default(),
                    FileType.Text to FileTypeConfig.default(),
                    FileType.Archive to FileTypeConfig.default(),
                    FileType.APK to FileTypeConfig.default()
                ),
                disableOnLowBattery = false
            )
        }
    }
}