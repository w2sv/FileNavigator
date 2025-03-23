package com.w2sv.domain.model

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.anggrayudi.storage.media.MediaType
import com.w2sv.core.domain.R
import com.w2sv.domain.model.PresetFileType.Media
import com.w2sv.domain.model.PresetFileType.NonMedia
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed interface PresetFileType : Parcelable, FileType {
    val labelRes: Int

    @IgnoredOnParcel
    override val ordinal: Int
        get() = ordinalsMap.getValue(this)

    sealed class Media(
        @StringRes override val labelRes: Int,
        @DrawableRes override val iconRes: Int,
        @ColorInt override val colorInt: Int,
        override val mediaType: MediaType,
        override val sourceTypes: List<SourceType>,
        override val fileExtensions: Collection<String>
    ) : PresetFileType, FileType {
        companion object {
            @JvmStatic
            val values: List<Media>
                get() = listOf(Image, Video, Audio)
        }
    }

    sealed class NonMedia(
        @StringRes override val labelRes: Int,
        @DrawableRes override val iconRes: Int,
        @ColorInt override val colorInt: Int,
        override val fileExtensions: Collection<String>
    ) : PresetFileType, NonMediaFileType() {
        companion object {
            @JvmStatic
            val values: List<NonMedia>
                get() = listOf(PDF, Text, Archive, APK, EBook)
        }
    }

    companion object {
        @JvmStatic
        val values: List<PresetFileType>
            get() = Media.values + NonMedia.values

        operator fun get(ordinal: Int): PresetFileType =
            values[ordinal]

        private val ordinalsMap by lazy { values.withIndex().associate { it.value to it.index } }
    }

    @Parcelize
    data object Image : Media(
        labelRes = R.string.image,
        iconRes = R.drawable.ic_image_24,
        colorInt = -4253137,
        mediaType = MediaType.IMAGE,
        sourceTypes = listOf(
            SourceType.Camera,
            SourceType.Screenshot,
            SourceType.OtherApp,
            SourceType.Download
        ),
        fileExtensions = setOf(
            "bmp", "cgm", "djv", "djvu", "gif", "ico", "ief", "jp2", "jpe", "jpeg", "jpg",
            "mac", "pbm", "pgm", "png", "pnm", "ppm", "ras", "rgb", "svg", "svgz", "tif",
            "tiff", "wbmp", "webp", "xbm", "xpm", "xwd"
        )
    )

    @Parcelize
    data object Video : Media(
        labelRes = R.string.video,
        iconRes = R.drawable.ic_video_file_24,
        colorInt = -13449,
        mediaType = MediaType.VIDEO,
        sourceTypes = listOf(SourceType.Camera, SourceType.OtherApp, SourceType.Download),
        fileExtensions = setOf(
            "3g2", "3gp", "asf", "avi", "f4v", "flv", "h261", "h263", "h264", "jpgv",
            "jpm", "m1v", "m2v", "m4u", "m4v", "mkv", "mov", "mp4", "mp4v", "mpg",
            "mpeg", "ogv", "qt", "ts", "vob", "webm", "wm", "wmv"
        )
    )

    @Parcelize
    data object Audio : Media(
        labelRes = R.string.audio,
        iconRes = R.drawable.ic_audio_file_24,
        colorInt = -891856,
        mediaType = MediaType.AUDIO,
        sourceTypes = listOf(SourceType.Recording, SourceType.OtherApp, SourceType.Download),
        fileExtensions = setOf(
            "aac", "aif", "aifc", "aiff", "au", "flac", "kar", "m3u", "m4a", "m4b", "m4p",
            "mp2", "mp3", "mpga", "oga", "ogg", "opus", "ra", "ram", "snd", "wav", "weba",
            "wma"
        )
    )

    @Parcelize
    data object PDF : NonMedia(
        R.string.pdf,
        R.drawable.ic_pdf_24,
        -14941188,
        listOf("pdf")
    )

    @Parcelize
    data object Text : NonMedia(
        R.string.text,
        R.drawable.ic_text_file_24,
        -1046887,
        setOf(
            "txt", "text", "asc", "csv", "xml", "json", "md", "doc", "docx", "odt",
            "wpd", "cfg", "log", "ini", "properties"
        )
    )

    @Parcelize
    data object Archive : NonMedia(
        R.string.archive,
        R.drawable.ic_folder_zip_24,
        -8232367,
        setOf(
            "zip", "rar", "tar", "7z", "gz", "bz2", "xz", "z", "iso", "cab", "tbz",
            "pkg", "deb", "rpm", "sit", "dmg", "jar", "war", "ear", "zipx", "tgz"
        )
    )

    @Parcelize
    data object APK : NonMedia(
        R.string.apk,
        R.drawable.ic_apk_file_24,
        -15410306,
        listOf("apk")
    )

    @Parcelize
    data object EBook : NonMedia(
        R.string.ebook,
        R.drawable.ic_book_24,
        -5728974,
        setOf(
            "epub", "azw", "azw1", "azw2", "azw3", "mobi", "iba", "rtf", "tpz", "mart",
            "tk3", "aep", "dnl", "ybk", "lit", "ebk", "prc", "kfx", "ava", "orb", "koob",
            "bpnueb", "pef", "vbk", "fkb", "bkk"
        )
    )
}
