package com.w2sv.domain.model.filetype

import android.content.Context
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig

interface StaticFileType {
    val mediaType: MediaType
    val sourceTypes: List<SourceType>
    val ordinal: Int
    val iconRes: Int

    fun label(context: Context): String

    fun defaultConfig(enabled: Boolean = true): FileTypeConfig =
        FileTypeConfig(
            enabled = enabled,
            sourceTypeConfigMap = sourceTypes.associateWith { SourceConfig() }
        )

    interface ExtensionSet : StaticFileType {
        val fileExtensions: Collection<String>
    }

    interface ExtensionConfigurable : StaticFileType {
        val defaultFileExtensions: Set<String>
    }

    sealed interface NonMedia : StaticFileType {
        override val mediaType get() = MediaType.DOWNLOADS
        override val sourceTypes get() = listOf(SourceType.Download)
    }
}
