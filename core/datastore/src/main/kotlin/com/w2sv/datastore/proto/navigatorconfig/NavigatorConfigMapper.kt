package com.w2sv.datastore.proto.navigatorconfig

import com.w2sv.datastore.AutoMoveConfigProto
import com.w2sv.datastore.CustomFileTypeProto
import com.w2sv.datastore.ExtensionConfigurableFileTypeProto
import com.w2sv.datastore.ExtensionPresetFileTypeProto
import com.w2sv.datastore.FileTypeConfigProto
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.SourceConfigProto
import com.w2sv.datastore.autoMoveConfigProto
import com.w2sv.datastore.customFileTypeProto
import com.w2sv.datastore.extensionConfigurableFileTypeProto
import com.w2sv.datastore.extensionPresetFileTypeProto
import com.w2sv.datastore.fileTypeConfigProto
import com.w2sv.datastore.navigatorConfigProto
import com.w2sv.datastore.proto.ProtoMapper
import com.w2sv.datastore.sourceConfigProto
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.PresetWrappingFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.model.navigatorconfig.FileTypeConfigMap
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import com.w2sv.kotlinutils.map

fun NavigatorConfig.toProto(hasBeenMigrated: Boolean): NavigatorConfigProto =
    NavigatorConfigMapper.toProto(this, hasBeenMigrated)

fun NavigatorConfigProto.toExternal(): NavigatorConfig =
    NavigatorConfigMapper.toExternal(this)

private object NavigatorConfigMapper : ProtoMapper<NavigatorConfigProto, NavigatorConfig> {
    override fun toExternal(proto: NavigatorConfigProto): NavigatorConfig =
        NavigatorConfig(
            fileTypeConfigMap = proto.fileTypeConfigMap(),
            showBatchMoveNotification = proto.showBatchMoveNotification,
            disableOnLowBattery = proto.disableOnLowBattery,
            startOnBoot = proto.startOnBoot
        )

    private fun NavigatorConfigProto.fileTypeConfigMap(): FileTypeConfigMap =
        buildMap {
            (extensionPresetFileTypesMap + extensionConfigurableFileTypesMap + customFileTypesMap)
                .forEach { (ordinal, proto) ->
                    put(
                        when (proto) {
                            is ExtensionPresetFileTypeProto -> (PresetFileType[ordinal] as PresetFileType.ExtensionSet).toFileType(
                                color = proto.color
                            )

                            is ExtensionConfigurableFileTypeProto ->
                                (PresetFileType[ordinal] as PresetFileType.ExtensionConfigurable).toFileType(
                                    color = proto.color,
                                    excludedExtensions = proto.excludedExtensionsList.toSet()
                                )

                            is CustomFileTypeProto -> CustomFileTypeMapper.toExternal(proto)
                            else -> error("Invalid proto type $proto")
                        },
                        FileTypeConfigMapper.toExternal(fileTypeToConfigMap.getValue(ordinal))
                    )
                }
        }

    override fun toProto(external: NavigatorConfig): NavigatorConfigProto =
        toProto(external, false)

    fun toProto(external: NavigatorConfig, hasBeenMigrated: Boolean): NavigatorConfigProto =
        navigatorConfigProto {
            external.fileTypeConfigMap.forEach { (fileType, fileTypeConfig) ->
                when (fileType) {
                    is PresetWrappingFileType.ExtensionSet -> this.extensionPresetFileTypes.put(
                        fileType.ordinal,
                        extensionPresetFileTypeProto { color = fileType.colorInt }
                    )

                    is PresetWrappingFileType.ExtensionConfigurable -> this.extensionConfigurableFileTypes.put(
                        fileType.ordinal,
                        extensionConfigurableFileTypeProto {
                            color = fileType.colorInt
                            excludedExtensions.addAll(fileType.excludedExtensions)
                        }
                    )

                    is CustomFileType -> this.customFileTypes.put(
                        fileType.ordinal,
                        CustomFileTypeMapper.toProto(fileType)
                    )
                }
                this.fileTypeToConfig.put(fileType.ordinal, FileTypeConfigMapper.toProto(fileTypeConfig))
            }
            this.showBatchMoveNotification = external.showBatchMoveNotification
            this.disableOnLowBattery = external.disableOnLowBattery
            this.startOnBoot = external.startOnBoot
            this.hasBeenMigrated = hasBeenMigrated
        }
}

private object FileTypeConfigMapper : ProtoMapper<FileTypeConfigProto, FileTypeConfig> {
    override fun toExternal(proto: FileTypeConfigProto): FileTypeConfig =
        FileTypeConfig(
            enabled = proto.enabled,
            sourceTypeConfigMap = proto.sourceTypeToConfigMap.map { (sourceTypeIndex, config) ->
                SourceType.entries[sourceTypeIndex] to SourceConfigMapper.toExternal(config)
            }
        )

    override fun toProto(external: FileTypeConfig): FileTypeConfigProto =
        fileTypeConfigProto {
            enabled = external.enabled
            sourceTypeToConfig.putAll(
                external.sourceTypeConfigMap.map { (type, config) ->
                    type.ordinal to SourceConfigMapper.toProto(config)
                }
            )
        }
}

private object SourceConfigMapper :
    ProtoMapper<SourceConfigProto, SourceConfig> {
    override fun toExternal(proto: SourceConfigProto): SourceConfig =
        SourceConfig(
            enabled = proto.enabled,
            quickMoveDestinations = proto.lastMoveDestinationsList.map { LocalDestination.parse(it) },
            autoMoveConfig = AutoMoveConfigMapper.toExternal(proto.autoMoveConfig)
        )

    override fun toProto(external: SourceConfig): SourceConfigProto =
        sourceConfigProto {
            this.enabled = external.enabled
            this.lastMoveDestinations.apply {
                clear()
                addAll(external.quickMoveDestinations.map { it.uriString })
            }
            this.autoMoveConfig = AutoMoveConfigMapper.toProto(external.autoMoveConfig)
        }
}

private object AutoMoveConfigMapper : ProtoMapper<AutoMoveConfigProto, AutoMoveConfig> {
    override fun toExternal(proto: AutoMoveConfigProto): AutoMoveConfig =
        AutoMoveConfig(
            enabled = proto.enabled,
            destination = if (proto.destination.isNotEmpty()) {
                LocalDestination.parse(proto.destination)
            } else {
                null
            }
        )

    override fun toProto(external: AutoMoveConfig): AutoMoveConfigProto =
        autoMoveConfigProto {
            enabled = external.enabled
            destination = external.destination?.uriString ?: ""
        }
}

private object CustomFileTypeMapper : ProtoMapper<CustomFileTypeProto, CustomFileType> {
    override fun toExternal(proto: CustomFileTypeProto): CustomFileType =
        CustomFileType(
            name = proto.name,
            fileExtensions = proto.extensionsList,
            colorInt = proto.color,
            ordinal = proto.ordinal
        )

    override fun toProto(external: CustomFileType): CustomFileTypeProto =
        customFileTypeProto {
            name = external.name
            extensions.addAll(external.fileExtensions)
            color = external.colorInt
            ordinal = external.ordinal
        }
}

private val MoveDestinationApi.uriString: String
    get() = documentUri.uri.toString()
