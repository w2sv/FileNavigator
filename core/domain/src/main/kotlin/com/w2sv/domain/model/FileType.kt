package com.w2sv.domain.model

import android.content.Context
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import kotlinx.parcelize.IgnoredOnParcel

sealed interface FileType : Parcelable {
    val mediaType: MediaType
    val sourceTypes: List<SourceType>
    val ordinal: Int
    val iconRes: Int

    @IgnoredOnParcel
    val asCustomTypeOrNull: CustomFileType? get() = this as? CustomFileType

    @IgnoredOnParcel
    val isMediaType: Boolean
        get() = this is PresetFileType.Media

    fun label(context: Context): String =
        when (this) {
            is PresetFileType -> context.getString(labelRes)
            is CustomFileType -> name
        }

    val colorInt: Int
}

internal fun FileType.defaultConfig(enabled: Boolean = true): FileTypeConfig =
    FileTypeConfig(
        enabled = enabled,
        sourceTypeConfigMap = sourceTypes.associateWith { SourceConfig() }
    )

sealed class NonMediaFileType : FileType {
    abstract val fileExtensions: List<String>

    override val mediaType = MediaType.DOWNLOADS
    override val sourceTypes = listOf(SourceType.Download)
}
