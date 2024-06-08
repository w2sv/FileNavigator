package com.w2sv.domain.model

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorLong
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.anggrayudi.storage.media.MediaType
import com.w2sv.core.domain.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed class FileType(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    @ColorInt val colorInt: Int,
    val simpleStorageMediaType: MediaType,
    val sourceTypes: List<SourceType>
) : Parcelable {

    val logIdentifier: String
        get() = this::class.java.simpleName

    @IgnoredOnParcel
    val isMediaType: Boolean
        get() = this is Media

    abstract fun matchesFileExtension(extension: String): Boolean

    sealed class Media(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int,
        @ColorLong colorLong: Long,
        mediaType: MediaType,
        sourceTypes: List<SourceType>
    ) : FileType(
        titleRes = labelRes,
        iconRes = iconRes,
        colorInt = colorLong.toInt(),
        simpleStorageMediaType = mediaType,
        sourceTypes = sourceTypes
    ) {

        override fun matchesFileExtension(extension: String): Boolean =
            true

        companion object {
            @JvmStatic
            val values: List<Media>
                get() = listOf(Image, Video, Audio)
        }
    }

    sealed class NonMedia(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int,
        @ColorLong colorLong: Long,
        private val fileExtensions: Set<String>,
    ) : FileType(
        titleRes = labelRes,
        iconRes = iconRes,
        colorInt = colorLong.toInt(),
        simpleStorageMediaType = MediaType.DOWNLOADS,
        sourceTypes = listOf(SourceType.Download)
    ) {

        override fun matchesFileExtension(extension: String): Boolean =
            fileExtensions.contains(extension)

        companion object {
            @JvmStatic
            val values: List<NonMedia>
                get() = listOf(PDF, Text, Archive, APK, EBook)
        }
    }

    @Parcelize
    data object Image : Media(
        labelRes = R.string.image,
        iconRes = R.drawable.ic_image_24,
        colorLong = 0xFFBF1A2F,
        mediaType = MediaType.IMAGE,
        sourceTypes = listOf(
            SourceType.Camera,
            SourceType.Screenshot,
            SourceType.OtherApp,
            SourceType.Download
        )
    )

    @Parcelize
    data object Video : Media(
        labelRes = R.string.video,
        iconRes = R.drawable.ic_video_file_24,
        colorLong = 0xFFFFCB77,
        mediaType = MediaType.VIDEO,
        sourceTypes = listOf(SourceType.Camera, SourceType.OtherApp, SourceType.Download)
    )

    @Parcelize
    data object Audio : Media(
        labelRes = R.string.audio,
        iconRes = R.drawable.ic_audio_file_24,
        colorLong = 0xFFF26430,
        mediaType = MediaType.AUDIO,
        sourceTypes = listOf(SourceType.Recording, SourceType.OtherApp, SourceType.Download)
    )

    @Parcelize
    data object PDF : NonMedia(
        R.string.pdf,
        R.drawable.ic_pdf_24,
        0xFF1c03fc,
        setOf("pdf")
    )

    @Parcelize
    data object Text : NonMedia(
        R.string.text,
        R.drawable.ic_text_file_24,
        0xFFF00699,
        setOf(
            "txt",
            "text",
            "asc",
            "csv",
            "xml",
            "json",
            "md",
            "doc",
            "docx",
            "odt",
            "wpd",
            "cfg",
            "log",
            "ini",
            "properties"
        )
    )

    @Parcelize
    data object Archive : NonMedia(
        R.string.archive,
        R.drawable.ic_folder_zip_24,
        0xFF826251,
        setOf(
            "zip",
            "rar",
            "tar",
            "7z",
            "gz",
            "bz2",
            "xz",
            "z",
            "iso",
            "cab",
            "tbz",
            "pkg",
            "deb",
            "rpm",
            "sit",
            "dmg",
            "jar",
            "war",
            "ear",
            "zipx",
            "tgz"
        )
    )

    @Parcelize
    data object APK : NonMedia(
        R.string.apk,
        R.drawable.ic_apk_file_24,
        0xFF14db7e,
        setOf("apk")
    )

    @Parcelize
    data object EBook : NonMedia(
        R.string.ebook,
        R.drawable.ic_book_24,
        0xFFa89532,
        setOf(
            "epub",
            "azw",
            "azw1",
            "azw2",
            "azw3",
            "mobi",
            "iba",
            "rtf",
            "tpz",
            "mart",
            "tk3",
            "aep",
            "dnl",
            "ybk",
            "lit",
            "ebk",
            "prc",
            "kfx",
            "ava",
            "orb",
            "koob",
            "epub",
            "bpnueb",
            "pef",
            "vbk",
            "fkb",
            "bkk",
        )
    )

    companion object {
        @JvmStatic
        val values: List<FileType>
            get() = Media.values + NonMedia.values
    }
}