package com.w2sv.datastore.proto.navigatorconfig

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.w2sv.datastore.FileTypeConfigProto
import com.w2sv.datastore.FileTypeProto
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.SourceConfigProto
import com.w2sv.datastore.SourceTypeProto
import com.w2sv.datastore.fileTypeConfigProto
import com.w2sv.datastore.navigatorConfigProto
import com.w2sv.datastore.sourceConfigProto
import java.io.InputStream
import java.io.OutputStream

internal object NavigatorConfigProtoSerializer : Serializer<NavigatorConfigProto> {
    override val defaultValue: NavigatorConfigProto
        get() = navigatorConfigProto {
            fileTypeConfigs.addAll(
                listOf(
                    defaultFileTypeConfigProto(
                        type = FileTypeProto.Image,
                        sourceKinds = listOf(
                            SourceTypeProto.Camera,
                            SourceTypeProto.Screenshot,
                            SourceTypeProto.OtherApp,
                            SourceTypeProto.Download,
                        )
                    ),
                    defaultFileTypeConfigProto(
                        type = FileTypeProto.Video,
                        sourceKinds = listOf(
                            SourceTypeProto.Camera,
                            SourceTypeProto.OtherApp,
                            SourceTypeProto.Download,
                        )
                    ),
                    defaultFileTypeConfigProto(
                        type = FileTypeProto.Audio,
                        sourceKinds = listOf(
                            SourceTypeProto.Recording,
                            SourceTypeProto.OtherApp,
                            SourceTypeProto.Download,
                        )
                    ),
                    defaultFileTypeConfigProto(
                        type = FileTypeProto.PDF
                    ),
                    defaultFileTypeConfigProto(
                        type = FileTypeProto.Text
                    ),
                    defaultFileTypeConfigProto(
                        type = FileTypeProto.Archive
                    ),
                    defaultFileTypeConfigProto(
                        type = FileTypeProto.APK
                    ),
                )
            )
            disableOnLowBattery = false
        }

    override suspend fun readFrom(input: InputStream): NavigatorConfigProto =
        try {
            // readFrom is already called on the data store background thread
            NavigatorConfigProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }

    override suspend fun writeTo(t: NavigatorConfigProto, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}

private fun defaultFileTypeConfigProto(
    type: FileTypeProto,
    sourceKinds: List<SourceTypeProto> = emptyList()
): FileTypeConfigProto =
    fileTypeConfigProto {
        this.type = type
        this.enabled = true
        this.sourceConfigs.addAll(
            sourceKinds.map { defaultSourceConfigProto(it) }
        )
    }

private fun defaultSourceConfigProto(type: SourceTypeProto): SourceConfigProto =
    sourceConfigProto {
        this.type = type
        this.enabled = true
    }