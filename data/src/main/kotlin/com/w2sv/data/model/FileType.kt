package com.w2sv.data.model

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorLong
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.anggrayudi.storage.media.MediaType
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.data.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed class FileType(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    @ColorInt val colorInt: Int,
    val simpleStorageMediaType: MediaType,
    sourceKinds: List<Source.Kind>,
) : Parcelable {

    val name: String = this::class.java.simpleName

    @IgnoredOnParcel
    val statusDSE
        get() = Status.getDSE(this)

    @IgnoredOnParcel
    val sources: List<Source> = sourceKinds.map { Source(this, it) }

    @IgnoredOnParcel
    val isMediaType: Boolean
        get() = this is Media

    abstract fun matchesFileExtension(extension: String): Boolean

    sealed class Media(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int,
        @ColorLong colorLong: Long,
        mediaType: MediaType,
        sourceKinds: List<Source.Kind>,
    ) : FileType(
        titleRes = labelRes,
        iconRes = iconRes,
        colorInt = colorLong.toInt(),
        simpleStorageMediaType = mediaType,
        sourceKinds = sourceKinds,
    ) {

        override fun matchesFileExtension(extension: String): Boolean =
            true

        @Parcelize
        data object Image : Media(
            labelRes = R.string.image,
            iconRes = R.drawable.ic_image_24,
            colorLong = 0xFFBF1A2F,
            mediaType = MediaType.IMAGE,
            sourceKinds = listOf(
                Source.Kind.Camera,
                Source.Kind.Screenshot,
                Source.Kind.Download,
                Source.Kind.OtherApp
            ),
        )

        @Parcelize
        data object Video : Media(
            labelRes = R.string.video,
            iconRes = R.drawable.ic_video_file_24,
            colorLong = 0xFFFFCB77,
            mediaType = MediaType.VIDEO,
            sourceKinds = listOf(
                Source.Kind.Camera,
                Source.Kind.Download,
                Source.Kind.OtherApp
            )
        )

        @Parcelize
        data object Audio : Media(
            labelRes = R.string.audio,
            iconRes = R.drawable.ic_audio_file_24,
            colorLong = 0xFFF26430,
            mediaType = MediaType.AUDIO,
            sourceKinds = listOf(
                Source.Kind.Recording,
                Source.Kind.Download,
                Source.Kind.OtherApp
            )
        )

        companion object {
            @JvmStatic
            fun getValues(): List<Media> = listOf(Image, Video, Audio)
        }
    }

    sealed class NonMedia(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int,
        @ColorLong colorLong: Long,
        private val fileExtensions: Set<String>
    ) : FileType(
        titleRes = labelRes,
        iconRes = iconRes,
        colorInt = colorLong.toInt(),
        simpleStorageMediaType = MediaType.DOWNLOADS,
        sourceKinds = listOf(
            Source.Kind.Download
        )
    ) {

        override fun matchesFileExtension(extension: String): Boolean =
            fileExtensions.contains(extension)

        @Parcelize
        data object PDF : NonMedia(
            R.string.pdf,
            R.drawable.ic_pdf_24,
            0xFFD6BA73,
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
            0xFFFCB07E,
            setOf("apk")
        )

        companion object {
            @JvmStatic
            fun getValues(): List<NonMedia> = listOf(PDF, Text, Archive, APK)
        }
    }

    enum class Status {
        Enabled,
        Disabled,
        DisabledDueToMediaAccessOnly;

        val isEnabled: Boolean get() = this == Enabled

        companion object {
            fun getDSE(fileType: FileType): DataStoreEntry.EnumValued<Status> =
                DataStoreEntry.EnumValued.Impl(
                    preferencesKey = intPreferencesKey(name = fileType.name),
                    defaultValue = Enabled
                )
        }
    }

    companion object {
        @JvmStatic
        fun getValues(): List<FileType> = Media.getValues() + NonMedia.getValues()
    }

    @Parcelize
    data class Source(val fileType: FileType, val kind: Kind) : Parcelable {

        private fun getPreferencesKeyContent(keySuffix: String): String =
            "${fileType.name}.$kind.$keySuffix"

        @IgnoredOnParcel
        val isEnabledDSE = DataStoreEntry.UniType.Impl(  // TODO: Remove for NonMedia
            booleanPreferencesKey(getPreferencesKeyContent("IS_ENABLED")),
            true
        )

        @IgnoredOnParcel
        val lastMoveDestinationDSE = DataStoreEntry.UriValued.Impl(
            stringPreferencesKey(getPreferencesKeyContent("LAST_MOVE_DESTINATION")),
            null
        )

        @DrawableRes
        fun getIconRes(): Int =
            when (kind) {
                Kind.Screenshot, Kind.Camera -> kind.iconRes
                else -> fileType.iconRes
            }

        enum class Kind(
            @StringRes val labelRes: Int,
            @DrawableRes val iconRes: Int
        ) {
            Camera(
                R.string.camera,
                R.drawable.ic_camera_24
            ),
            Screenshot(
                R.string.screenshot,
                R.drawable.ic_screenshot_24
            ),
            Recording(
                R.string.recording,
                R.drawable.ic_mic_24
            ),
            Download(
                R.string.download,
                R.drawable.ic_file_download_24
            ),
            OtherApp(
                R.string.third_party_app,
                R.drawable.ic_apps_24
            )
        }
    }
}