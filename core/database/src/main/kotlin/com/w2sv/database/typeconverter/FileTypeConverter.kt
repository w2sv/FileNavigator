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
            is CustomFileType -> fileType.name
        }

    @TypeConverter
    fun toFileType(name: String): FileType =
        when (name) {
            "Image" -> PresetFileType.Image
            "Audio" -> PresetFileType.Audio
            "Video" -> PresetFileType.Video
            "PDF" -> PresetFileType.PDF
            "APK" -> PresetFileType.APK
            "Text" -> PresetFileType.Text
            "Archive" -> PresetFileType.Archive
            "EBook" -> PresetFileType.EBook
            else -> CustomFileType(name, emptyList(), -1) // TODO
        }
}
