package com.w2sv.domain.model.filetype

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt

typealias AnyPresetWrappingFileType = PresetWrappingFileType<*>

sealed interface PresetWrappingFileType<T : PresetFileType> : FileType {
    val presetFileType: T

    data class ExtensionSet(override val presetFileType: PresetFileType.ExtensionSet, @ColorInt override val colorInt: Int) :
        StaticFileType.ExtensionSet by presetFileType, FileType, PresetWrappingFileType<PresetFileType.ExtensionSet> {

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(presetFileType.ordinal)
            parcel.writeInt(colorInt)
        }

        override fun describeContents(): Int =
            0

        companion object CREATOR : Parcelable.Creator<ExtensionSet> {
            override fun createFromParcel(parcel: Parcel): ExtensionSet {
                val fileTypeOrdinal = parcel.readInt()
                val colorInt = parcel.readInt()
                return ExtensionSet(PresetFileType.Companion[fileTypeOrdinal] as PresetFileType.ExtensionSet, colorInt)
            }

            override fun newArray(size: Int): Array<ExtensionSet?> =
                arrayOfNulls(size)
        }
    }

    data class ExtensionConfigurable(
        override val presetFileType: PresetFileType.ExtensionConfigurable,
        @ColorInt override val colorInt: Int,
        val excludedExtensions: Set<String>
    ) : StaticFileType.ExtensionConfigurable by presetFileType, FileType, PresetWrappingFileType<PresetFileType.ExtensionConfigurable> {

        override val fileExtensions: Collection<String> by lazy {
            defaultFileExtensions - excludedExtensions
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(presetFileType.ordinal)
            parcel.writeInt(colorInt)
            parcel.writeStringList(excludedExtensions.toList())
        }

        override fun describeContents(): Int =
            0

        companion object CREATOR : Parcelable.Creator<ExtensionConfigurable> {
            override fun createFromParcel(parcel: Parcel): ExtensionConfigurable {
                val fileTypeOrdinal = parcel.readInt()
                val colorInt = parcel.readInt()
                val excludedExtensions = emptyList<String>()
                parcel.readStringList(excludedExtensions)
                return ExtensionConfigurable(
                    presetFileType = PresetFileType.Companion[fileTypeOrdinal] as PresetFileType.ExtensionConfigurable,
                    colorInt = colorInt,
                    excludedExtensions = excludedExtensions.toSet()
                )
            }

            override fun newArray(size: Int): Array<ExtensionConfigurable?> =
                arrayOfNulls(size)
        }
    }
}
