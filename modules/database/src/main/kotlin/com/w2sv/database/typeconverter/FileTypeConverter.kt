package com.w2sv.database.typeconverter

import androidx.room.TypeConverter
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetFileType

internal object FileTypeConverter {

    @TypeConverter
    fun fromFileType(fileType: FileType): String =
        when (fileType) {
            is FileType.Preset -> fileType.presetFileType.name
            is FileType.Custom -> fileType.serialized()
        }

    @TypeConverter
    fun toFileType(string: String): FileType =
        PresetFileType.entries.firstOrNull { it.name == string }?.toFileType()
            ?: deserializedCustomFileType(string)
}

private fun FileType.Custom.serialized(): String =
    listOf(name, ordinal, colorInt).joinToString(DELIMITER_CHAR)

private fun deserializedCustomFileType(string: String): FileType {
    val (name, ordinal, colorInt) = string.split(DELIMITER_CHAR)
    return FileType.custom(
        name = name,
        fileExtensions = emptyList(),
        colorInt = colorInt.toInt(),
        ordinal = ordinal.toInt()
    )
}

private const val DELIMITER_CHAR = ","
