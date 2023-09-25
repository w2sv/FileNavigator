package com.w2sv.data.model

import android.content.Context
import android.os.Parcelable
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
    val colorLong: Long,
    val mediaType: MediaType,
    sourceKinds: List<Source.Kind>,
) : Parcelable {

    val identifier: String = this::class.java.simpleName

    val status by lazy {
        Status.StoreEntry(this)
    }

    val sources: List<Source> = sourceKinds.map { Source(this, it) }

    val isMediaType: Boolean get() = this is Media

    abstract fun matchesFileExtension(extension: String): Boolean

    sealed class Media(
        @StringRes labelRes: Int,
        @StringRes val fileDeclarationRes: Int,
        @DrawableRes iconRes: Int,
        color: Long,
        simpleStorageType: MediaType,
        sourceKinds: List<Source.Kind>,
        val fileExtensions: Set<String>? = null,
        val ignoreFileExtensionsOf: Media? = null
    ) : FileType(
        titleRes = labelRes,
        iconRes = iconRes,
        colorLong = color,
        mediaType = simpleStorageType,
        sourceKinds = sourceKinds,
    ) {

        override fun matchesFileExtension(extension: String): Boolean =
            when {
                fileExtensions != null -> fileExtensions.contains(extension)

                ignoreFileExtensionsOf != null -> !ignoreFileExtensionsOf.fileExtensions!!.contains(
                    extension
                )

                else -> true
            }

        @Parcelize
        data object Image : Media(
            labelRes = R.string.image,
            fileDeclarationRes = R.string.image,
            iconRes = R.drawable.ic_image_24,
            color = 0xFFBF1A2F,
            simpleStorageType = MediaType.IMAGE,
            sourceKinds = listOf(
                Source.Kind.Camera,
                Source.Kind.Screenshot,
                Source.Kind.Download,
                Source.Kind.OtherApp
            ),
            ignoreFileExtensionsOf = GIF
        )

        @Parcelize
        data object GIF : Media(
            labelRes = R.string.gif,
            fileDeclarationRes = R.string.gif,
            iconRes = R.drawable.ic_gif_box_24,
            color = 0xFF49C6E5,
            simpleStorageType = MediaType.IMAGE,
            sourceKinds = listOf(
                Source.Kind.Download,
                Source.Kind.OtherApp
            ),
            fileExtensions = setOf("gif", "GIF", "giff")
        )

        @Parcelize
        data object Video : Media(
            labelRes = R.string.video,
            fileDeclarationRes = R.string.video,
            iconRes = R.drawable.ic_video_file_24,
            color = 0xFFFFCB77,
            simpleStorageType = MediaType.VIDEO,
            sourceKinds = listOf(
                Source.Kind.Camera,
                Source.Kind.Download,
                Source.Kind.OtherApp
            )
        )

        @Parcelize
        data object Audio : Media(
            labelRes = R.string.audio,
            fileDeclarationRes = R.string.audio_file,
            iconRes = R.drawable.ic_audio_file_24,
            color = 0xFFF26430,
            simpleStorageType = MediaType.AUDIO,
            sourceKinds = listOf(
                Source.Kind.Download,
                Source.Kind.OtherApp
            )
        )

        companion object {
            val values: List<Media> = listOf(Image, Video, GIF, Audio)
        }
    }

    sealed class NonMedia(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int,
        color: Long,
        val fileExtensions: Set<String>
    ) : FileType(
        titleRes = labelRes,
        iconRes = iconRes,
        colorLong = color,
        mediaType = MediaType.DOWNLOADS,
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
            val values: List<NonMedia> = listOf(PDF, Text, Archive, APK)
        }
    }

    enum class Status {
        Enabled,
        Disabled,
        DisabledDueToMediaAccessOnly,
        DisabledDueToNoFileAccess;

        val isEnabled: Boolean get() = this == Enabled

        class StoreEntry(fileType: FileType) : DataStoreEntry.EnumValued.Impl<Status>(
            preferencesKey = intPreferencesKey(name = fileType.identifier),
            defaultValue = DisabledDueToNoFileAccess
        )
    }

    companion object {
        val values: List<FileType> = Media.values + NonMedia.values
    }

    @Parcelize
    class Source(val fileType: FileType, val kind: Kind) : Parcelable {

        private fun getPreferencesKeyContent(keySuffix: String): String =
            "${fileType.identifier}.$kind.$keySuffix"

        @IgnoredOnParcel
        val isEnabled by lazy {
            object : DataStoreEntry.UniType.Impl<Boolean>(
                booleanPreferencesKey(getPreferencesKeyContent("IS_ENABLED")),
                true
            ) {}
        }

        @IgnoredOnParcel
        val defaultDestination by lazy {
            object : DataStoreEntry.UriValued.Impl(
                stringPreferencesKey(getPreferencesKeyContent("DEFAULT_DESTINATION")),
                null
            ) {}
        }

        fun getTitle(context: Context): String =
            when (kind) {
                Kind.Screenshot -> "Screenshot"
                Kind.Camera -> {
                    if (fileType == Media.Image)
                        "Photo"
                    else
                        "Video"
                }

                Kind.Download -> "${context.getString(fileType.titleRes)} Download"
                Kind.OtherApp -> "External App ${context.getString(fileType.titleRes)}"
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

fun <FT : FileType> Iterable<FT>.filterEnabled(statusMap: Map<FileType.Status.StoreEntry, FileType.Status>): List<FT> =
    filter { statusMap.getValue(it.status).isEnabled }