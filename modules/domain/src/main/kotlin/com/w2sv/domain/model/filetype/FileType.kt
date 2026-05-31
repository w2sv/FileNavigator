package com.w2sv.domain.model.filetype

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.anggrayudi.storage.media.MediaType
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import kotlin.random.Random
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * User-visible file type as configured by the navigator.
 *
 * The sealed variants model the mutation capabilities of each kind directly:
 * fixed presets only allow color edits, configurable presets additionally allow
 * extension exclusions, and custom file types additionally own their name and
 * extension set.
 */
sealed interface FileType : Parcelable {
    val id: FileTypeId

    @get:ColorInt
    val colorInt: Int

    val fileExtensions: Set<String>

    @IgnoredOnParcel
    val definition: FileTypeDefinition
        get() = FileTypeDefinition[id]

    val mediaType: MediaType
        get() = definition.mediaType

    val sourceTypes: List<SourceType>
        get() = definition.sourceTypes

    val ordinal: Int
        get() = id.ordinal

    @get:DrawableRes
    val iconRes: Int
        get() = definition.iconRes

    val isMediaType: Boolean
        get() = id is FileTypeId.Preset && mediaType != MediaType.DOWNLOADS

    val presetTypeOrNull: PresetFileType?
        get() = (this as? Preset)?.presetFileType

    val logIdentifier: String
        get() = when (this) {
            is Custom -> name
            is Preset -> presetFileType.toString()
        }

    fun name(context: Context): String =
        when (this) {
            is Custom -> name
            is Preset -> context.getString(checkNotNull(definition.labelRes))
        }

    fun withColor(@ColorInt colorInt: Int): FileType

    /**
     * Builds the default navigator configuration for this file type.
     */
    fun defaultConfig(enabled: Boolean = true): FileTypeConfig =
        FileTypeConfig(
            fileType = this,
            enabled = enabled,
            sourceTypeConfigMap = sourceTypes.associateWith { SourceConfig() }
        )

    /**
     * Built-in preset file type. Presets keep their identity in [PresetFileType]
     * and only store user-editable state on the concrete [FileType] variant.
     */
    sealed interface Preset : FileType {
        val presetFileType: PresetFileType

        override val id: FileTypeId
            get() = FileTypeId.Preset(presetFileType)
    }

    /**
     * Built-in preset whose extensions are static and cannot be changed by the user.
     */
    @Parcelize
    data class FixedPreset(override val presetFileType: PresetFileType, @ColorInt override val colorInt: Int) : Preset {
        override val fileExtensions: Set<String>
            get() = presetFileType.fileExtensions

        override fun withColor(colorInt: Int): FixedPreset =
            copy(colorInt = colorInt)
    }

    /**
     * Built-in preset whose default extensions can be selectively disabled.
     */
    @Parcelize
    data class ConfigurablePreset(
        override val presetFileType: PresetFileType,
        @ColorInt override val colorInt: Int,
        val excludedExtensions: Set<String>
    ) : Preset {
        val availableFileExtensions: Set<String>
            get() = presetFileType.fileExtensions

        override val fileExtensions: Set<String>
            get() = availableFileExtensions - excludedExtensions

        fun withExcludedExtensions(excludedExtensions: Set<String>): ConfigurablePreset =
            copy(excludedExtensions = excludedExtensions)

        override fun withColor(colorInt: Int): ConfigurablePreset =
            copy(colorInt = colorInt)
    }

    /**
     * User-created file type with user-owned name, color, and extension set.
     */
    @Parcelize
    data class Custom(
        val name: String,
        override val fileExtensions: Set<String>,
        @ColorInt override val colorInt: Int,
        override val ordinal: Int
    ) : FileType {
        @IgnoredOnParcel
        override val id: FileTypeId = FileTypeId.Custom(ordinal)

        fun withCustomName(name: String): Custom =
            copy(name = name)

        fun withFileExtensions(fileExtensions: Collection<String>): Custom =
            copy(fileExtensions = fileExtensions.toSet())

        override fun withColor(colorInt: Int): Custom =
            copy(colorInt = colorInt)
    }

    companion object {
        fun preset(
            presetFileType: PresetFileType,
            @ColorInt color: Int = EMPTY_COLOR_INT,
            excludedExtensions: Set<String> = emptySet()
        ): Preset {
            require(presetFileType.extensionsAreConfigurable || excludedExtensions.isEmpty()) {
                "${presetFileType.name} does not support excluded extensions."
            }

            val colorInt = selectColor(color, presetFileType.defaultColorInt)
            return if (presetFileType.extensionsAreConfigurable) {
                ConfigurablePreset(
                    presetFileType = presetFileType,
                    colorInt = colorInt,
                    excludedExtensions = excludedExtensions
                )
            } else {
                FixedPreset(
                    presetFileType = presetFileType,
                    colorInt = colorInt
                )
            }
        }

        fun custom(
            name: String,
            fileExtensions: Collection<String>,
            @ColorInt colorInt: Int,
            ordinal: Int
        ): Custom =
            Custom(
                name = name,
                fileExtensions = fileExtensions.toSet(),
                colorInt = colorInt,
                ordinal = ordinal
            )

        /**
         * Creates an unsaved custom file type draft with a fresh custom ordinal.
         */
        fun newEmptyCustom(existingFileTypes: Collection<FileType>): Custom =
            custom(
                name = "",
                fileExtensions = emptyList(),
                colorInt = randomColor(),
                ordinal = maxOf(MIN_CUSTOM_ORDINAL, existingFileTypes.maxOfOrNull { it.ordinal }?.let { it + 1 } ?: MIN_CUSTOM_ORDINAL)
            )
    }
}

private const val EMPTY_COLOR_INT = 0
private const val MIN_CUSTOM_ORDINAL = 1_000

@ColorInt
private fun selectColor(@ColorInt storedColor: Int, @ColorInt defaultColor: Int): Int =
    if (storedColor != EMPTY_COLOR_INT) storedColor else defaultColor

@ColorInt
private fun randomColor(): Int =
    Color.rgb(
        Random.nextInt(256),
        Random.nextInt(256),
        Random.nextInt(256)
    )
