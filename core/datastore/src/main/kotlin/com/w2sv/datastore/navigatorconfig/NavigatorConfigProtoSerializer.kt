package com.w2sv.datastore.navigatorconfig

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.w2sv.datastore.FileTypeProto
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.fileTypeProto
import com.w2sv.datastore.navigatorConfigProto
import java.io.InputStream
import java.io.OutputStream

internal object NavigatorConfigProtoSerializer : Serializer<NavigatorConfigProto> {
    override val defaultValue: NavigatorConfigProto
        get() = navigatorConfigProto {
            fileTypes.addAll(
                listOf(
                    defaultFileTypeProto(
                        kind = FileTypeProto.Kind.Image,
                        sourceKinds = listOf(
                            FileTypeProto.Source.Kind.Camera,
                            FileTypeProto.Source.Kind.Screenshot,
                            FileTypeProto.Source.Kind.OtherApp,
                            FileTypeProto.Source.Kind.Download,
                        )
                    ),
                    defaultFileTypeProto(
                        kind = FileTypeProto.Kind.Video,
                        sourceKinds = listOf(
                            FileTypeProto.Source.Kind.Camera,
                            FileTypeProto.Source.Kind.OtherApp,
                            FileTypeProto.Source.Kind.Download,
                        )
                    ),
                    defaultFileTypeProto(
                        kind = FileTypeProto.Kind.Audio,
                        sourceKinds = listOf(
                            FileTypeProto.Source.Kind.Recording,
                            FileTypeProto.Source.Kind.OtherApp,
                            FileTypeProto.Source.Kind.Download,
                        )
                    ),
                    defaultFileTypeProto(
                        kind = FileTypeProto.Kind.PDF
                    ),
                    defaultFileTypeProto(
                        kind = FileTypeProto.Kind.Text
                    ),
                    defaultFileTypeProto(
                        kind = FileTypeProto.Kind.Archive
                    ),
                    defaultFileTypeProto(
                        kind = FileTypeProto.Kind.APK
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

private fun defaultFileTypeProto(
    kind: FileTypeProto.Kind,
    sourceKinds: List<FileTypeProto.Source.Kind> = emptyList()
): FileTypeProto =
    fileTypeProto {
        this.kind = kind
        this.enabled = true
        this.sources.addAll(
            sourceKinds.map { defaultFileTypeProtoSource(it) }
        )
    }

private fun defaultFileTypeProtoSource(kind: FileTypeProto.Source.Kind): FileTypeProto.Source =
    FileTypeProto.Source.newBuilder()
        .apply {
            this.kind = kind
            this.enabled = true
        }
        .build()