package com.w2sv.datastore.proto.navigatorconfig

import com.w2sv.datastore.AutoMoveConfigProto
import com.w2sv.datastore.CustomFileTypeProto
import com.w2sv.datastore.FileTypeConfigProto
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.SourceConfigProto
import com.w2sv.datastore.autoMoveConfigProto
import com.w2sv.datastore.customFileTypeProto
import com.w2sv.datastore.fileTypeConfigProto
import com.w2sv.datastore.navigatorConfigProto
import com.w2sv.datastore.proto.ProtoMapper
import com.w2sv.datastore.sourceConfigProto
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
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
            fileTypeConfigMap = proto.fileTypeToConfigMap.map { (ordinal, fileTypeConfigProto) ->
                fileType(ordinal, proto) to FileTypeConfigMapper.toExternal(fileTypeConfigProto)
            },
            showBatchMoveNotification = proto.showBatchMoveNotification,
            disableOnLowBattery = proto.disableOnLowBattery,
            startOnBoot = proto.startOnBoot
        )

    private fun fileType(ordinal: Int, proto: NavigatorConfigProto): FileType =
        try {
            PresetFileType[ordinal]
        } catch (_: IndexOutOfBoundsException) {
            CustomFileTypeMapper.toExternal(proto.customFileTypesList.first { it.ordinal == ordinal })
        }

    override fun toProto(external: NavigatorConfig): NavigatorConfigProto =
        toProto(external, false)

    fun toProto(external: NavigatorConfig, hasBeenMigrated: Boolean): NavigatorConfigProto =
        navigatorConfigProto {
            this.fileTypeToConfig.putAll(
                external.fileTypeConfigMap.map { (fileType, fileTypeConfig) ->
                    fileType.asCustomTypeOrNull?.let { customFileType ->
                        this.customFileTypes.add(CustomFileTypeMapper.toProto(customFileType))
                    }
                    fileType.ordinal to FileTypeConfigMapper.toProto(fileTypeConfig)
                }
            )
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
        CustomFileType(proto.name, proto.extensionsList, proto.ordinal)

    override fun toProto(external: CustomFileType): CustomFileTypeProto =
        customFileTypeProto {
            name = external.name
            extensions.addAll(external.fileExtensions)
            ordinal = external.ordinal
        }
}

private val MoveDestinationApi.uriString: String
    get() = documentUri.uri.toString()
