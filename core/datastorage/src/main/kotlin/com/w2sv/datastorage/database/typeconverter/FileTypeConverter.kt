package com.w2sv.datastorage.database.typeconverter

import androidx.room.TypeConverter
import com.w2sv.domain.model.FileType

object FileTypeConverter {
    @TypeConverter
    fun fromFileType(fileType: FileType): String =
        when (fileType) {
            FileType.Image -> "Image"
            FileType.Audio -> "Audio"
            FileType.Video -> "Video"
            FileType.PDF -> "PDF"
            FileType.APK -> "APK"
            FileType.Text -> "Text"
            FileType.Archive -> "Archive"
        }

    @TypeConverter
    fun toFileType(name: String): FileType =
        when (name) {
            "Image" -> FileType.Image
            "Audio" -> FileType.Audio
            "Video" -> FileType.Video
            "PDF" -> FileType.PDF
            "APK" -> FileType.APK
            "Text" -> FileType.Text
            "Archive" -> FileType.Archive
            else -> throw NoSuchElementException()
        }
}