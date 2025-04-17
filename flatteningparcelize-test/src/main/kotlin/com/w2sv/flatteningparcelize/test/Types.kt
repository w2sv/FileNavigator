package com.w2sv.flatteningparcelize.test

import android.os.Parcelable
import com.w2sv.flatteningparcelize.FlatteningParcelize
import kotlinx.parcelize.Parcelize

interface ExtensionSetStaticFileType : Parcelable {
    val type: String
    val name: String

    @Parcelize
    data class Impl(
        override val type: String,
        override val name: String
    ) : ExtensionSetStaticFileType
}

@FlatteningParcelize
data class ExtensionSetFileType(
    private val delegate: ExtensionSetStaticFileType,
    val colorInt: Int
) : ExtensionSetStaticFileType by delegate
