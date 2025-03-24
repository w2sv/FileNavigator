package com.w2sv.database.typeconverter

import androidx.room.TypeConverter
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.PresetFileType

internal object FileTypeConverter {

    @TypeConverter
    fun fromFileType(fileType: FileType): String =
        when (fileType) {
            PresetFileType.Image -> "Image"
            PresetFileType.Audio -> "Audio"
            PresetFileType.Video -> "Video"
            PresetFileType.PDF -> "PDF"
            PresetFileType.APK -> "APK"
            PresetFileType.Text -> "Text"
            PresetFileType.Archive -> "Archive"
            PresetFileType.EBook -> "EBook"
            is CustomFileType -> fileType.serialized()
            else -> error("Should not happen") // TODO
        }

    @TypeConverter
    fun toFileType(string: String): FileType =
        when (string) {
            "Image" -> PresetFileType.Image
            "Audio" -> PresetFileType.Audio
            "Video" -> PresetFileType.Video
            "PDF" -> PresetFileType.PDF
            "APK" -> PresetFileType.APK
            "Text" -> PresetFileType.Text
            "Archive" -> PresetFileType.Archive
            "EBook" -> PresetFileType.EBook
            else -> CustomFileType.deserialized(string)
        }
}

private fun CustomFileType.serialized(): String =
    "$name$DELIMITER_CHAR$colorInt"

private fun CustomFileType.Companion.deserialized(string: String): CustomFileType {
    val (name, colorInt) = string.split(DELIMITER_CHAR)
    return CustomFileType(
        name = name,
        fileExtensions = emptyList(),
        colorInt = colorInt.toInt(),
        ordinal = -1
    )
}

private const val DELIMITER_CHAR = ","
