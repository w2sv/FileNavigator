package com.w2sv.datastore.proto.navigatorconfig

import com.w2sv.datastore.AutoMoveConfigProto
import com.w2sv.datastore.FileTypeConfigProto
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.SourceConfigProto
import com.w2sv.datastore.autoMoveConfigProto
import com.w2sv.datastore.fileTypeConfigProto
import com.w2sv.datastore.navigatorConfigProto
import com.w2sv.datastore.proto.ProtoMapper
import com.w2sv.datastore.sourceConfigProto
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig

internal object NavigatorConfigMapper : ProtoMapper<NavigatorConfigProto, NavigatorConfig> {
    override fun toExternal(proto: NavigatorConfigProto): NavigatorConfig =
        NavigatorConfig(
            fileTypeConfigMap = proto.fileTypeToConfigMap.entries.associate { (fileTypeIndex, configProto) ->
                FileType.values[fileTypeIndex] to FileTypeConfigMapper.toExternal(configProto)
            },
            showBatchMoveNotification = proto.showBatchMoveNotification,
            disableOnLowBattery = proto.disableOnLowBattery,
            startOnBoot = proto.startOnBoot
        )

    override fun toProto(external: NavigatorConfig): NavigatorConfigProto =
        toProto(external = external, hasBeenMigrated = null)

    fun toProto(external: NavigatorConfig, hasBeenMigrated: Boolean?): NavigatorConfigProto =
        navigatorConfigProto {
            fileTypeToConfig.putAll(
                external.fileTypeConfigMap.entries.associate { (fileType, config) ->
                    FileType.values.indexOf(fileType) to FileTypeConfigMapper.toProto(config)
                }
            )
            showBatchMoveNotification = external.showBatchMoveNotification
            disableOnLowBattery = external.disableOnLowBattery
            startOnBoot = external.startOnBoot
            hasBeenMigrated?.let {
                this.hasBeenMigrated = it
            }
        }
}

private object FileTypeConfigMapper : ProtoMapper<FileTypeConfigProto, FileTypeConfig> {
    override fun toExternal(proto: FileTypeConfigProto): FileTypeConfig =
        FileTypeConfig(
            enabled = proto.enabled,
            sourceTypeConfigMap = proto.sourceTypeToConfigMap.entries.associate { (sourceTypeIndex, config) ->
                SourceType.entries[sourceTypeIndex] to SourceConfigMapper.toExternal(config)
            },
        )

    override fun toProto(external: FileTypeConfig): FileTypeConfigProto =
        fileTypeConfigProto {
            enabled = external.enabled
            sourceTypeToConfig.putAll(
                external.sourceTypeConfigMap.entries.associate { (type, config) ->
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
            lastMoveDestinations = proto.lastMoveDestinationsList.map { MoveDestination.parse(it) },
            autoMoveConfig = AutoMoveConfigMapper.toExternal(proto.autoMoveConfig)
        )

    override fun toProto(external: SourceConfig): SourceConfigProto =
        sourceConfigProto {
            this.enabled = external.enabled
            this.lastMoveDestinations.apply {
                clear()
                addAll(external.lastMoveDestinations.map { it.toString() })
            }
            this.autoMoveConfig = AutoMoveConfigMapper.toProto(external.autoMoveConfig)
        }
}

private object AutoMoveConfigMapper : ProtoMapper<AutoMoveConfigProto, AutoMoveConfig> {
    override fun toExternal(proto: AutoMoveConfigProto): AutoMoveConfig = AutoMoveConfig(
        enabled = proto.enabled,
        destination = if (proto.destination.isNotEmpty()) {
            MoveDestination.parse(proto.destination)
        } else {
            null
        }
    )

    override fun toProto(external: AutoMoveConfig): AutoMoveConfigProto = autoMoveConfigProto {
        enabled = external.enabled
        destination = external.destination?.toString() ?: ""
    }
}