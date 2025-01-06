package com.w2sv.datastore.proto

internal interface ProtoMapper<Proto, External> {
    fun toExternal(proto: Proto): External
    fun toProto(external: External): Proto
}
