package com.w2sv.domain.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.core.os.ParcelCompat
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
}

interface ExtensionSetStaticFileType : StaticFileType {
    val fileExtensions: Collection<String>

    fun toFileType(@ColorInt color: Int): FileType {}
}

interface ExtensionConfigurableStaticFileType : StaticFileType {
    val defaultFileExtensions: Set<String>
}

interface FileType : ExtensionSetStaticFileType {
    val colorInt: Int

    @IgnoredOnParcel
    val asCustomTypeOrNull: CustomFileType?
        get() = this as? CustomFileType

    @IgnoredOnParcel
    val isMediaType: Boolean
        get() = this is StaticPresetFileType.Media
}

data class ExtensionSetFileType(private val extensionSetStaticFileType: ExtensionSetStaticFileType, @ColorInt override val colorInt: Int) :
    ExtensionSetStaticFileType by extensionSetStaticFileType, FileType {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(extensionSetStaticFileType, flags)
        parcel.writeInt(colorInt)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ExtensionSetFileType> {
        override fun createFromParcel(parcel: Parcel): ExtensionSetFileType {
            val fileType: ExtensionSetFileType = ParcelCompat.readParcelable(
                parcel,
                ExtensionSetFileType::class.java.classLoader,
                ExtensionSetFileType::class.java
            )!!
            val excludedExtensions = parcel.readInt()
            return ExtensionSetFileType(fileType, excludedExtensions)
        }

        override fun newArray(size: Int): Array<ExtensionSetFileType?> =
            arrayOfNulls(size)
    }
}

data class ExtensionConfigurableFileType(
    private val extensionConfigurableStaticFileType: ExtensionConfigurableStaticFileType,
    @ColorInt override val colorInt: Int,
    val excludedExtensions: Collection<String>
) :
    ExtensionConfigurableStaticFileType by extensionConfigurableStaticFileType, FileType {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(extensionConfigurableStaticFileType, flags)
        parcel.writeInt(colorInt)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ExtensionSetFileType> {
        override fun createFromParcel(parcel: Parcel): ExtensionSetFileType {
            val fileType: ExtensionSetFileType = ParcelCompat.readParcelable(
                parcel,
                ExtensionSetFileType::class.java.classLoader,
                ExtensionSetFileType::class.java
            )!!
            val excludedExtensions = parcel.readInt()
            return ExtensionSetFileType(fileType, excludedExtensions)
        }

        override fun newArray(size: Int): Array<ExtensionSetFileType?> =
            arrayOfNulls(size)
    }
}

internal fun StaticFileType.defaultConfig(enabled: Boolean = true): FileTypeConfig =
    FileTypeConfig(
        enabled = enabled,
        sourceTypeConfigMap = sourceTypes.associateWith { SourceConfig() }
    )
