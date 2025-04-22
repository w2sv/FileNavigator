package com.w2sv.database.typeconverter

import androidx.room.TypeConverter
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.PresetWrappingFileType

internal object FileTypeConverter {

    @TypeConverter
    fun fromFileType(fileType: FileType): String =
        when (fileType) {
            is PresetWrappingFileType<*> -> {
                when (fileType.presetFileType) {
                    PresetFileType.Image -> "Image"
                    PresetFileType.Audio -> "Audio"
                    PresetFileType.Video -> "Video"
                    PresetFileType.PDF -> "PDF"
                    PresetFileType.APK -> "APK"
                    PresetFileType.Text -> "Text"
                    PresetFileType.Archive -> "Archive"
                    PresetFileType.EBook -> "EBook"
                }
            }

            is CustomFileType -> fileType.serialized()
        }

    @TypeConverter
    fun toFileType(string: String): FileType =
        when (string) {
            "Image" -> PresetFileType.Image.toFileType()
            "Audio" -> PresetFileType.Audio.toFileType()
            "Video" -> PresetFileType.Video.toFileType()
            "PDF" -> PresetFileType.PDF.toFileType()
            "APK" -> PresetFileType.APK.toFileType()
            "Text" -> PresetFileType.Text.toFileType()
            "Archive" -> PresetFileType.Archive.toFileType()
            "EBook" -> PresetFileType.EBook.toFileType()
            else -> CustomFileType.deserialized(string)
        }
}

private fun CustomFileType.serialized(): String =
    listOf(name, ordinal, colorInt).joinToString(DELIMITER_CHAR)

private fun CustomFileType.Companion.deserialized(string: String): CustomFileType {
    val (name, ordinal, colorInt) = string.split(DELIMITER_CHAR)
    return CustomFileType(
        name = name,
        fileExtensions = emptyList(),
        colorInt = colorInt.toInt(),
        ordinal = ordinal.toInt()
    )
}

private const val DELIMITER_CHAR = ","
