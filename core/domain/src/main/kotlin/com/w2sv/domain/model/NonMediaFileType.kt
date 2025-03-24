package com.w2sv.domain.model

import com.anggrayudi.storage.media.MediaType

private val nonMediaSourceTypes = listOf(SourceType.Download)

sealed interface NonMediaFileType : FileType {
    override val mediaType get() = MediaType.DOWNLOADS
    override val sourceTypes get() = nonMediaSourceTypes

    sealed interface WithExtensions: NonMediaFileType, FileExtensionsHolder
}
