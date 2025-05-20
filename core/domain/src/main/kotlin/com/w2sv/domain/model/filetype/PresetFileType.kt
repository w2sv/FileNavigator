package com.w2sv.domain.model.filetype

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import com.anggrayudi.storage.media.MediaType
import com.w2sv.core.common.R

sealed interface PresetFileType : StaticFileType {
    val labelRes: Int
    val defaultColorInt: Int

    override val ordinal: Int
        get() = ordinalsMap.getValue(this)

    override fun label(context: Context): String =
        context.getString(labelRes)

    fun toDefaultFileType(): AnyPresetWrappingFileType =
        when (this) {
            is ExtensionSet -> toFileType()
            is ExtensionConfigurable -> toFileType()
        }

    sealed interface ExtensionSet : PresetFileType, StaticFileType.ExtensionSet {
        fun toFileType(@ColorInt color: Int = EMPTY_COLOR_INT): PresetWrappingFileType.ExtensionSet =
            PresetWrappingFileType.ExtensionSet(
                presetFileType = this,
                colorInt = selectColor(
                    storedColor = color,
                    defaultColor = defaultColorInt
                )
            )
    }

    sealed interface ExtensionConfigurable : PresetFileType, StaticFileType.ExtensionConfigurable {
        fun toFileType(
            @ColorInt color: Int = EMPTY_COLOR_INT,
            excludedExtensions: Set<String> = emptySet()
        ): PresetWrappingFileType.ExtensionConfigurable =
            PresetWrappingFileType.ExtensionConfigurable(
                presetFileType = this,
                colorInt = selectColor(
                    storedColor = color,
                    defaultColor = defaultColorInt
                ),
                excludedExtensions = excludedExtensions
            )
    }

    sealed class Media(
        @StringRes override val labelRes: Int,
        @DrawableRes override val iconRes: Int,
        @ColorInt override val defaultColorInt: Int,
        override val mediaType: MediaType,
        override val sourceTypes: List<SourceType>,
        override val fileExtensions: Collection<String>
    ) : ExtensionSet {
        companion object {
            @JvmStatic
            val values: List<Media>
                get() = listOf(Image, Video, Audio)
        }
    }

    sealed interface NonMedia : PresetFileType, StaticFileType.NonMedia {

        sealed class ExtensionSet(
            @StringRes override val labelRes: Int,
            @DrawableRes override val iconRes: Int,
            @ColorInt override val defaultColorInt: Int,
            override val fileExtensions: Collection<String>
        ) : NonMedia, PresetFileType.ExtensionSet

        sealed class ExtensionConfigurable(
            @StringRes override val labelRes: Int,
            @DrawableRes override val iconRes: Int,
            @ColorInt override val defaultColorInt: Int,
            override val defaultFileExtensions: Set<String>
        ) : NonMedia, PresetFileType.ExtensionConfigurable {
            companion object {
                @JvmStatic
                val values: List<ExtensionConfigurable>
                    get() = listOf(Text, Archive, EBook)
            }
        }

        companion object {
            @JvmStatic
            val values: List<NonMedia>
                get() = listOf(PDF, Text, Archive, APK, EBook)
        }
    }

    data object Image : Media(
        labelRes = R.string.image,
        iconRes = R.drawable.ic_image_24,
        defaultColorInt = -4253137,
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

    data object Video : Media(
        labelRes = R.string.video,
        iconRes = R.drawable.ic_video_file_24,
        defaultColorInt = -13449,
        mediaType = MediaType.VIDEO,
        sourceTypes = listOf(SourceType.Camera, SourceType.OtherApp, SourceType.Download),
        fileExtensions = setOf(
            "3g2", "3gp", "asf", "avi", "f4v", "flv", "h261", "h263", "h264", "jpgv",
            "jpm", "m1v", "m2v", "m4u", "m4v", "mkv", "mov", "mp4", "mp4v", "mpg",
            "mpeg", "ogv", "qt", "ts", "vob", "webm", "wm", "wmv"
        )
    )

    data object Audio : Media(
        labelRes = R.string.audio,
        iconRes = R.drawable.ic_audio_file_24,
        defaultColorInt = -891856,
        mediaType = MediaType.AUDIO,
        sourceTypes = listOf(SourceType.Recording, SourceType.OtherApp, SourceType.Download),
        fileExtensions = setOf(
            "aac", "aif", "aifc", "aiff", "au", "flac", "kar", "m3u", "m4a", "m4b", "m4p",
            "mp2", "mp3", "mpga", "oga", "ogg", "opus", "ra", "ram", "snd", "wav", "weba",
            "wma"
        )
    )

    data object PDF : NonMedia.ExtensionSet(
        R.string.pdf,
        R.drawable.ic_pdf_24,
        -14941188,
        setOf("pdf")
    )

    data object Text : NonMedia.ExtensionConfigurable(
        R.string.text,
        R.drawable.ic_text_file_24,
        -1046887,
        setOf(
            "txt", "text", "asc", "csv", "xml", "json", "md", "doc", "docx", "odt",
            "wpd", "cfg", "log", "ini", "properties", "html"
        )
    )

    data object Archive : NonMedia.ExtensionConfigurable(
        R.string.archive,
        R.drawable.ic_folder_zip_24,
        -8232367,
        setOf(
            "zip", "rar", "tar", "7z", "gz", "bz2", "xz", "z", "iso", "cab", "tbz",
            "pkg", "deb", "rpm", "sit", "dmg", "jar", "war", "ear", "zipx", "tgz"
        )
    )

    data object APK : NonMedia.ExtensionSet(
        R.string.apk,
        R.drawable.ic_apk_file_24,
        -15410306,
        setOf("apk")
    )

    data object EBook : NonMedia.ExtensionConfigurable(
        R.string.ebook,
        R.drawable.ic_book_24,
        -5728974,
        setOf(
            "epub", "azw", "azw1", "azw2", "azw3", "mobi", "iba", "rtf", "tpz", "mart",
            "tk3", "aep", "dnl", "ybk", "lit", "ebk", "prc", "kfx", "ava", "orb", "koob",
            "bpnueb", "pef", "vbk", "fkb", "bkk"
        )
    )

    companion object {
        @JvmStatic
        val values: List<PresetFileType>
            get() = Media.values + NonMedia.values

        operator fun get(ordinal: Int): PresetFileType =
            values[ordinal]

        @VisibleForTesting
        val ordinalsMap by lazy { values.withIndex().associate { it.value to it.index } }
    }
}

private const val EMPTY_COLOR_INT = 0

@ColorInt
private fun selectColor(@ColorInt storedColor: Int, @ColorInt defaultColor: Int): Int =
    if (storedColor != EMPTY_COLOR_INT) storedColor else defaultColor
