package com.w2sv.data.storage.database.typeconverters

import androidx.room.TypeConverter
import com.w2sv.data.model.FileType

object FileTypeConverter {
    @TypeConverter
    fun fromDate(fileType: FileType): String =
        when (fileType) {
            FileType.Media.Image -> "Image"
            FileType.Media.Audio -> "Audio"
            FileType.Media.Video -> "Video"
            FileType.NonMedia.PDF -> "PDF"
            FileType.NonMedia.APK -> "APK"
            FileType.NonMedia.Text -> "Text"
            FileType.NonMedia.Archive -> "Archive"
        }

    @TypeConverter
    fun toFileType(name: String): FileType =
        when (name) {
            "Image" -> FileType.Media.Image
            "Audio" -> FileType.Media.Audio
            "Video" -> FileType.Media.Video
            "PDF" -> FileType.NonMedia.PDF
            "APK" -> FileType.NonMedia.APK
            "Text" -> FileType.NonMedia.APK
            "Archive" -> FileType.NonMedia.Archive
            else -> throw NoSuchElementException()
        }
}