package com.w2sv.domain.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import kotlinx.parcelize.IgnoredOnParcel

interface StaticFileType : Parcelable {
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
}

sealed interface FileType : StaticFileType.ExtensionSet {
    val colorInt: Int

    @IgnoredOnParcel
    val asCustomTypeOrNull: CustomFileType?
        get() = this as? CustomFileType

    @IgnoredOnParcel
    val wrappedPresetType: PresetFileType?
        get() = (this as? PresetWrapper<*>)?.presetFileType

    @IgnoredOnParcel
    val isMediaType: Boolean
        get() = wrappedPresetType is PresetFileType.Media
}

sealed interface PresetWrapper<T : PresetFileType> {
    val presetFileType: T
}

data class ExtensionSetFileType(override val presetFileType: PresetFileType.ExtensionSet, @ColorInt override val colorInt: Int) :
    StaticFileType.ExtensionSet by presetFileType, FileType, PresetWrapper<PresetFileType.ExtensionSet> {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(presetFileType.ordinal)
        parcel.writeInt(colorInt)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ExtensionSetFileType> {
        override fun createFromParcel(parcel: Parcel): ExtensionSetFileType {
            val fileTypeOrdinal = parcel.readInt()
            val colorInt = parcel.readInt()
            return ExtensionSetFileType(PresetFileType[fileTypeOrdinal] as PresetFileType.ExtensionSet, colorInt)
        }

        override fun newArray(size: Int): Array<ExtensionSetFileType?> =
            arrayOfNulls(size)
    }
}

data class ExtensionConfigurableFileType(
    override val presetFileType: PresetFileType.ExtensionConfigurable,
    @ColorInt override val colorInt: Int,
    val excludedExtensions: Set<String>
) : StaticFileType.ExtensionConfigurable by presetFileType, FileType, PresetWrapper<PresetFileType.ExtensionConfigurable> {

    override val fileExtensions: Collection<String> by lazy {
        defaultFileExtensions - excludedExtensions
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(presetFileType.ordinal)
        parcel.writeInt(colorInt)
        parcel.writeStringList(excludedExtensions.toList())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ExtensionConfigurableFileType> {
        override fun createFromParcel(parcel: Parcel): ExtensionConfigurableFileType {
            val fileTypeOrdinal = parcel.readInt()
            val colorInt = parcel.readInt()
            val excludedExtensions = emptyList<String>()
            parcel.readStringList(excludedExtensions)
            return ExtensionConfigurableFileType(
                presetFileType = PresetFileType[fileTypeOrdinal] as PresetFileType.ExtensionConfigurable,
                colorInt = colorInt,
                excludedExtensions = excludedExtensions.toSet()
            )
        }

        override fun newArray(size: Int): Array<ExtensionConfigurableFileType?> =
            arrayOfNulls(size)
    }
}
