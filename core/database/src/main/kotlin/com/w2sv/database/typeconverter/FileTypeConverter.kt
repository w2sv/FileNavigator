package com.w2sv.database.typeconverter

import androidx.room.TypeConverter
import com.w2sv.domain.model.FileTypeKind

object FileTypeConverter {
    @TypeConverter
    fun fromFileType(fileType: FileTypeKind): String =
        when (fileType) {
            FileTypeKind.Image -> "Image"
            FileTypeKind.Audio -> "Audio"
            FileTypeKind.Video -> "Video"
            FileTypeKind.PDF -> "PDF"
            FileTypeKind.APK -> "APK"
            FileTypeKind.Text -> "Text"
            FileTypeKind.Archive -> "Archive"
        }

    @TypeConverter
    fun toFileType(name: String): FileTypeKind =
        when (name) {
            "Image" -> FileTypeKind.Image
            "Audio" -> FileTypeKind.Audio
            "Video" -> FileTypeKind.Video
            "PDF" -> FileTypeKind.PDF
            "APK" -> FileTypeKind.APK
            "Text" -> FileTypeKind.Text
            "Archive" -> FileTypeKind.Archive
            else -> throw NoSuchElementException()
        }
}