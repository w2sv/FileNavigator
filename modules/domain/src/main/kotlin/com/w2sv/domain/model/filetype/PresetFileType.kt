package com.w2sv.domain.model.filetype

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.anggrayudi.storage.media.MediaType
import com.w2sv.modules.common.R

/**
 * Built-in file type definition.
 *
 * Presets are static identities with default metadata: label, icon, media store
 * bucket, source types, default color and built-in extensions. User state such
 * as the selected color or excluded extensions is stored on the concrete
 * [FileType.Preset] variant.
 */
enum class PresetFileType(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    @ColorInt val defaultColorInt: Int,
    val mediaType: MediaType,
    val sourceTypes: List<SourceType>,
    val fileExtensions: Set<String>,
    val extensionsAreConfigurable: Boolean = false
) {
    Image(
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
    ),

    Video(
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
    ),

    Audio(
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
    ),

    PDF(
        labelRes = R.string.pdf,
        iconRes = R.drawable.ic_pdf_24,
        defaultColorInt = -14941188,
        mediaType = MediaType.DOWNLOADS,
        sourceTypes = listOf(SourceType.Download),
        fileExtensions = setOf("pdf")
    ),

    Text(
        labelRes = R.string.text,
        iconRes = R.drawable.ic_text_file_24,
        defaultColorInt = -1046887,
        mediaType = MediaType.DOWNLOADS,
        sourceTypes = listOf(SourceType.Download),
        fileExtensions = setOf(
            "txt", "text", "asc", "csv", "xml", "json", "md", "doc", "docx", "odt",
            "wpd", "cfg", "log", "ini", "properties", "html"
        ),
        extensionsAreConfigurable = true
    ),

    Archive(
        labelRes = R.string.archive,
        iconRes = R.drawable.ic_folder_zip_24,
        defaultColorInt = -8232367,
        mediaType = MediaType.DOWNLOADS,
        sourceTypes = listOf(SourceType.Download),
        fileExtensions = setOf(
            "zip", "rar", "tar", "7z", "gz", "bz2", "xz", "z", "iso", "cab", "tbz",
            "pkg", "deb", "rpm", "sit", "dmg", "jar", "war", "ear", "zipx", "tgz"
        ),
        extensionsAreConfigurable = true
    ),

    APK(
        labelRes = R.string.apk,
        iconRes = R.drawable.ic_apk_file_24,
        defaultColorInt = -15410306,
        mediaType = MediaType.DOWNLOADS,
        sourceTypes = listOf(SourceType.Download),
        fileExtensions = setOf("apk")
    ),

    EBook(
        labelRes = R.string.ebook,
        iconRes = R.drawable.ic_book_24,
        defaultColorInt = -5728974,
        mediaType = MediaType.DOWNLOADS,
        sourceTypes = listOf(SourceType.Download),
        fileExtensions = setOf(
            "epub", "azw", "azw1", "azw2", "azw3", "mobi", "iba", "rtf", "tpz", "mart",
            "tk3", "aep", "dnl", "ybk", "lit", "ebk", "prc", "kfx", "ava", "orb", "koob",
            "bpnueb", "pef", "vbk", "fkb", "bkk", "xtc"
        ),
        extensionsAreConfigurable = true
    );

    fun toFileType(@ColorInt color: Int = 0, excludedExtensions: Set<String> = emptySet()): FileType.Preset =
        FileType.preset(
            presetFileType = this,
            color = color,
            excludedExtensions = excludedExtensions
        )

    companion object {

        @JvmStatic
        val mediaEntries: List<PresetFileType> by lazy {
            entries.filter { it.mediaType != MediaType.DOWNLOADS }
        }

        @JvmStatic
        val downloadEntries: List<PresetFileType> by lazy {
            entries.filter { it.mediaType == MediaType.DOWNLOADS }
        }

        @JvmStatic
        val configurableNonMediaValues: List<PresetFileType>
            get() = downloadEntries.filter { it.extensionsAreConfigurable }

        operator fun get(ordinal: Int): PresetFileType =
            entries[ordinal]
    }
}
