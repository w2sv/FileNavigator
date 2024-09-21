package com.w2sv.datastore.proto.navigatorconfig

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import java.io.InputStream
import java.io.OutputStream

internal object NavigatorConfigProtoSerializer : Serializer<NavigatorConfigProto> {
    override val defaultValue: NavigatorConfigProto by lazy {
        NavigatorConfig.default.toProto(false)
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