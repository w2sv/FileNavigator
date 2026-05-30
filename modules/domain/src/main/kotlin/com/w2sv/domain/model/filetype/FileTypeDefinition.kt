package com.w2sv.domain.model.filetype

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.anggrayudi.storage.media.MediaType
import com.w2sv.modules.common.R

/**
 * Static presentation and matching metadata for a [FileTypeId].
 *
 * Definitions are derived from identity and should not be persisted as part of
 * user configuration. They provide the default icon, optional preset label,
 * media bucket and source types needed by navigation and UI code. Custom file
 * type labels come from [FileType.Custom.name], so custom definitions do
 * not carry a UI fallback label.
 */
data class FileTypeDefinition(
    @StringRes val labelRes: Int?,
    @DrawableRes val iconRes: Int,
    @ColorInt val defaultColorInt: Int,
    val mediaType: MediaType,
    val sourceTypes: List<SourceType>
) {
    companion object {
        operator fun get(id: FileTypeId): FileTypeDefinition =
            when (id) {
                is FileTypeId.Custom -> custom
                is FileTypeId.Preset -> presetDefinitions.getValue(id.presetFileType)
            }

        private val custom by lazy {
            FileTypeDefinition(
                labelRes = null,
                iconRes = R.drawable.ic_custom_file_type_24,
                defaultColorInt = 0,
                mediaType = MediaType.DOWNLOADS,
                sourceTypes = listOf(SourceType.Download)
            )
        }

        private val presetDefinitions: Map<PresetFileType, FileTypeDefinition> by lazy {
            PresetFileType.entries.associateWith { presetFileType ->
                FileTypeDefinition(
                    labelRes = presetFileType.labelRes,
                    iconRes = presetFileType.iconRes,
                    defaultColorInt = presetFileType.defaultColorInt,
                    mediaType = presetFileType.mediaType,
                    sourceTypes = presetFileType.sourceTypes
                )
            }
        }
    }
}
