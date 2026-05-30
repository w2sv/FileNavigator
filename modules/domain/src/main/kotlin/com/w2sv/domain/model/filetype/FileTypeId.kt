package com.w2sv.domain.model.filetype

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Stable identity of a [FileType].
 *
 * Use this as the key for navigator configuration and persistence lookups. It
 * deliberately excludes mutable settings such as color, name and extensions so
 * edits to a file type do not change which configuration entry it belongs to.
 */
sealed interface FileTypeId : Parcelable {
    val ordinal: Int

    /**
     * Identity of one built-in preset file type.
     */
    @Parcelize
    @JvmInline
    value class Preset(val presetFileType: PresetFileType) : FileTypeId {
        override val ordinal: Int
            get() = presetFileType.ordinal
    }

    /**
     * Identity of a user-created file type.
     *
     * The ordinal is the persisted custom id and must remain stable for the
     * lifetime of that custom type.
     */
    @Parcelize
    @JvmInline
    value class Custom(override val ordinal: Int) : FileTypeId
}
