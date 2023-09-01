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
import com.w2sv.common.notifications.NotificationChannelProperties
import com.w2sv.data.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed class FileType(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val colorLong: Long,
    val mediaType: MediaType,
    sourceKinds: List<SourceKind>,
) : Parcelable {

    val identifier: String = this::class.java.simpleName

    val notificationChannel = NotificationChannelProperties(identifier, identifier)

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
        sourceKinds: List<SourceKind>,
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
                SourceKind.Camera,
                SourceKind.Screenshot,
                SourceKind.Download,
                SourceKind.OtherApp
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
                SourceKind.Download,
                SourceKind.OtherApp
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
                SourceKind.Camera,
                SourceKind.Download,
                SourceKind.OtherApp
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
                SourceKind.Download,
                SourceKind.OtherApp
            )
        )

        companion object {
            val all: List<Media> get() = listOf(Image, Video, GIF, Audio)
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
            SourceKind.Download
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
            val all: List<NonMedia> get() = listOf(PDF, Text, Archive, APK)
        }
    }

    enum class Status {
        Enabled,
        Disabled,
        DisabledForMediaAccessOnly,
        DisabledForNoFileAccess;

        val isEnabled: Boolean get() = this == Enabled

        class StoreEntry(fileType: FileType) : DataStoreEntry.EnumValued.Impl<Status>(
            intPreferencesKey(fileType.identifier),
            DisabledForNoFileAccess
        )
    }

    companion object {
        val values: List<FileType> = Media.all + NonMedia.all
    }

    enum class SourceKind(
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

    @Parcelize
    class Source(val fileType: FileType, val kind: SourceKind) : Parcelable {

        private fun getPreferencesKeyTitle(keySuffix: String): String =
            "${fileType.identifier}.$kind.$keySuffix"

        inner class IsEnabled : DataStoreEntry.UniType.Impl<Boolean>(
            booleanPreferencesKey(getPreferencesKeyTitle("IS_ENABLED")),
            true
        )

        inner class DefaultDestination : DataStoreEntry.UriValued.Impl(
            stringPreferencesKey(getPreferencesKeyTitle("DEFAULT_DESTINATION")),
            null
        )

        inner class DefaultDestinationIsLocked : DataStoreEntry.UniType.Impl<Boolean>(
            booleanPreferencesKey(getPreferencesKeyTitle("DEFAULT_DESTINATION_IS_LOCKED")),
            false
        )

        @IgnoredOnParcel
        val isEnabled by lazy {
            IsEnabled()
        }

        @IgnoredOnParcel
        val defaultDestination by lazy {
            DefaultDestination()
        }

        @IgnoredOnParcel
        val defaultDestinationIsLocked by lazy {
            DefaultDestinationIsLocked()
        }

        fun getTitle(context: Context): String =
            when (kind) {
                SourceKind.Screenshot -> "Screenshot"
                SourceKind.Camera -> {
                    if (fileType == Media.Image)
                        "Photo"
                    else
                        "Video"
                }

                SourceKind.Download -> "${context.getString(fileType.titleRes)} Download"
                SourceKind.OtherApp -> "External App ${context.getString(fileType.titleRes)}"
            }
    }
}