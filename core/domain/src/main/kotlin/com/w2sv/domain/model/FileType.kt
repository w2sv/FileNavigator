package com.w2sv.domain.model

import kotlinx.parcelize.IgnoredOnParcel

sealed interface FileType : StaticFileType.ExtensionSet {
    val colorInt: Int

    @IgnoredOnParcel
    val asCustomTypeOrNull: CustomFileType?
        get() = this as? CustomFileType

    @IgnoredOnParcel
    val wrappedPresetType: PresetFileType?
        get() = (this as? PresetWrappingFileType<*>)?.presetFileType

    @IgnoredOnParcel
    val isMediaType: Boolean
        get() = wrappedPresetType is PresetFileType.Media
}
