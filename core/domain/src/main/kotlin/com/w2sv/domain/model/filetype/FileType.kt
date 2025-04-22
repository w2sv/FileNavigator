package com.w2sv.domain.model.filetype

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel

sealed interface FileType : StaticFileType.ExtensionSet, Parcelable {
    val colorInt: Int

    @IgnoredOnParcel
    val asExtensionConfigurableTypeOrNull: PresetWrappingFileType.ExtensionConfigurable?
        get() = this as? PresetWrappingFileType.ExtensionConfigurable

    @IgnoredOnParcel
    val wrappedPresetTypeOrNull: PresetFileType?
        get() = (this as? PresetWrappingFileType<*>)?.presetFileType

    @IgnoredOnParcel
    val isMediaType: Boolean
        get() = wrappedPresetTypeOrNull is PresetFileType.Media
}
