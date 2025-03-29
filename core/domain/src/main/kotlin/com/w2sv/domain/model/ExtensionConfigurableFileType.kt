package com.w2sv.domain.model

interface ExtensionConfigurableFileType : FileType {
    val defaultFileExtensions: Set<String>
}
