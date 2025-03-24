package com.w2sv.domain.model

import android.content.Context
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import kotlinx.parcelize.IgnoredOnParcel

interface FileType : Parcelable {
    val mediaType: MediaType
    val sourceTypes: List<SourceType>
    val ordinal: Int
    val iconRes: Int
    val colorInt: Int

    @IgnoredOnParcel
    val asCustomTypeOrNull: CustomFileType? get() = this as? CustomFileType

    @IgnoredOnParcel
    val isMediaType: Boolean
        get() = this is PresetFileType.Media

    fun label(context: Context): String
}

internal fun FileType.defaultConfig(enabled: Boolean = true): FileTypeConfig =
    FileTypeConfig(
        enabled = enabled,
        sourceTypeConfigMap = sourceTypes.associateWith { SourceConfig() }
    )

