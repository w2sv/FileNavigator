package com.w2sv.datastore.proto.navigatorconfig

import android.net.Uri
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
            disableOnLowBattery = proto.disableOnLowBattery
        )

    override fun toProto(external: NavigatorConfig): NavigatorConfigProto =
        navigatorConfigProto {
            fileTypeToConfig.putAll(
                external.fileTypeConfigMap.entries.associate { (fileType, config) ->
                    FileType.values.indexOf(fileType) to FileTypeConfigMapper.toProto(config)
                }
            )
            disableOnLowBattery = external.disableOnLowBattery
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

//private object FileTypeMapper : ProtoMapper<FileTypeProto, FileType> {
//    override fun toExternal(proto: FileTypeProto): FileType =
//        when (proto) {
//            FileTypeProto.Image -> FileType.Image
//            FileTypeProto.Video -> FileType.Video
//            FileTypeProto.Audio -> FileType.Audio
//            FileTypeProto.PDF -> FileType.PDF
//            FileTypeProto.Text -> FileType.Text
//            FileTypeProto.Archive -> FileType.Archive
//            FileTypeProto.APK -> FileType.APK
//            FileTypeProto.UNRECOGNIZED -> throw IllegalArgumentException("Unrecognized FileTypeProto")
//        }
//
//    override fun toProto(external: FileType): FileTypeProto =
//        when (external) {
//            FileType.Image -> FileTypeProto.Image
//            FileType.Video -> FileTypeProto.Video
//            FileType.Audio -> FileTypeProto.Audio
//            FileType.PDF -> FileTypeProto.PDF
//            FileType.Text -> FileTypeProto.Text
//            FileType.Archive -> FileTypeProto.Archive
//            FileType.APK -> FileTypeProto.APK
//        }
//}

private object SourceConfigMapper :
    ProtoMapper<SourceConfigProto, SourceConfig> {
    override fun toExternal(proto: SourceConfigProto): SourceConfig =
        SourceConfig(
            enabled = proto.enabled,
            lastMoveDestinations = proto.lastMoveDestinationsList.map { Uri.parse(it) },
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

//private object SourceTypeMapper :
//    ProtoMapper<SourceTypeProto, SourceType> {
//    override fun toExternal(proto: SourceTypeProto): SourceType =
//        when (proto) {
//            SourceTypeProto.Camera -> SourceType.Camera
//            SourceTypeProto.Screenshot -> SourceType.Screenshot
//            SourceTypeProto.Recording -> SourceType.Recording
//            SourceTypeProto.Download -> SourceType.Download
//            SourceTypeProto.OtherApp -> SourceType.OtherApp
//            SourceTypeProto.UNRECOGNIZED -> throw IllegalArgumentException("Unrecognized SourceTypeProto")
//        }
//
//    override fun toProto(external: SourceType): SourceTypeProto =
//        when (external) {
//            SourceType.Camera -> SourceTypeProto.Camera
//            SourceType.Screenshot -> SourceTypeProto.Screenshot
//            SourceType.Recording -> SourceTypeProto.Recording
//            SourceType.Download -> SourceTypeProto.Download
//            SourceType.OtherApp -> SourceTypeProto.OtherApp
//        }
//}

private object AutoMoveConfigMapper : ProtoMapper<AutoMoveConfigProto, AutoMoveConfig> {
    override fun toExternal(proto: AutoMoveConfigProto): AutoMoveConfig = AutoMoveConfig(
        enabled = proto.enabled,
        destination = if (proto.destination.isNotEmpty()) {
            Uri.parse(proto.destination)
        } else {
            null
        }
    )

    override fun toProto(external: AutoMoveConfig): AutoMoveConfigProto = autoMoveConfigProto {
        enabled = external.enabled
        destination = external.destination?.toString() ?: ""
    }
}