package com.w2sv.domain.model

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.anggrayudi.storage.media.MediaType
import com.w2sv.core.domain.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed class FileType(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    @ColorInt val colorInt: Int,
    val simpleStorageMediaType: MediaType,
    val sourceTypes: List<SourceType>
) : Parcelable {

    val isMediaType: Boolean
        get() = this is Media

    @IgnoredOnParcel
    val ordinal: Int by lazy {
        values.indexOf(this)
    }

    sealed class Media(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int,
        @ColorInt colorInt: Int,
        mediaType: MediaType,
        sourceTypes: List<SourceType>
    ) : FileType(
        labelRes = labelRes,
        iconRes = iconRes,
        colorInt = colorInt,
        simpleStorageMediaType = mediaType,
        sourceTypes = sourceTypes
    ) {
        companion object {
            @JvmStatic
            val values: List<Media>
                get() = listOf(Image, Video, Audio)
        }
    }

    sealed class NonMedia(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int,
        @ColorInt colorInt: Int,
        val fileExtensions: Set<String>
    ) : FileType(
        labelRes = labelRes,
        iconRes = iconRes,
        colorInt = colorInt,
        simpleStorageMediaType = MediaType.DOWNLOADS,
        sourceTypes = listOf(SourceType.Download)
    ) {
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
        colorInt = -4253137,
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
        colorInt = -13449,
        mediaType = MediaType.VIDEO,
        sourceTypes = listOf(SourceType.Camera, SourceType.OtherApp, SourceType.Download)
    )

    @Parcelize
    data object Audio : Media(
        labelRes = R.string.audio,
        iconRes = R.drawable.ic_audio_file_24,
        colorInt = -891856,
        mediaType = MediaType.AUDIO,
        sourceTypes = listOf(SourceType.Recording, SourceType.OtherApp, SourceType.Download)
    )

    @Parcelize
    data object PDF : NonMedia(
        R.string.pdf,
        R.drawable.ic_pdf_24,
        -14941188,
        setOf("pdf")
    )

    @Parcelize
    data object Text : NonMedia(
        R.string.text,
        R.drawable.ic_text_file_24,
        -1046887,
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
        -8232367,
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
        -15410306,
        setOf("apk")
    )

    @Parcelize
    data object EBook : NonMedia(
        R.string.ebook,
        R.drawable.ic_book_24,
        -5728974,
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
            "bkk"
        )
    )

    companion object {
        @JvmStatic
        val values: List<FileType>
            get() = Media.values + NonMedia.values
    }
}
