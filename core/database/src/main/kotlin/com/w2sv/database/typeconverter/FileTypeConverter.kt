package com.w2sv.database.typeconverter

import androidx.room.TypeConverter
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.StaticPresetFileType

internal object FileTypeConverter {

    @TypeConverter
    fun fromFileType(fileType: FileType): String =
        when (fileType) {
            StaticPresetFileType.Image -> "Image"
            StaticPresetFileType.Audio -> "Audio"
            StaticPresetFileType.Video -> "Video"
            StaticPresetFileType.PDF -> "PDF"
            StaticPresetFileType.APK -> "APK"
            StaticPresetFileType.Text -> "Text"
            StaticPresetFileType.Archive -> "Archive"
            StaticPresetFileType.EBook -> "EBook"
            is CustomFileType -> fileType.serialized()
            else -> error("Should not happen") // TODO
        }

    @TypeConverter
    fun toFileType(string: String): FileType =
        when (string) {
            "Image" -> StaticPresetFileType.Image
            "Audio" -> StaticPresetFileType.Audio
            "Video" -> StaticPresetFileType.Video
            "PDF" -> StaticPresetFileType.PDF
            "APK" -> StaticPresetFileType.APK
            "Text" -> StaticPresetFileType.Text
            "Archive" -> StaticPresetFileType.Archive
            "EBook" -> StaticPresetFileType.EBook
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
