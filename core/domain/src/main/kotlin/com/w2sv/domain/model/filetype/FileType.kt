package com.w2sv.domain.model.filetype

import android.os.Parcelable
import com.w2sv.common.util.logIdentifier

sealed interface FileType : StaticFileType.ExtensionSet, Parcelable {
    val colorInt: Int

    val asExtensionConfigurableTypeOrNull: PresetWrappingFileType.ExtensionConfigurable?
        get() = this as? PresetWrappingFileType.ExtensionConfigurable

    val wrappedPresetTypeOrNull: PresetFileType?
        get() = (this as? PresetWrappingFileType<*>)?.presetFileType

    val isMediaType: Boolean
        get() = wrappedPresetTypeOrNull is PresetFileType.Media

    val logIdentifier: String
        get() = when (this) {
            is CustomFileType -> name
            is AnyPresetWrappingFileType -> presetFileType.logIdentifier
        }
}
