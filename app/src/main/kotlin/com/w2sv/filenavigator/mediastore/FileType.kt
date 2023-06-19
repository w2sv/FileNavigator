package com.w2sv.filenavigator.mediastore

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.DataStoreVariable
import kotlinx.parcelize.Parcelize

sealed class FileType(
    val storageType: com.anggrayudi.storage.media.MediaType,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val color: Color,
    sourceKinds: List<SourceKind>,
    override val defaultValue: Boolean = false
) : DataStoreVariable<Boolean>, Parcelable {

    companion object {
        val all: List<FileType> get() = Media.all + NonMedia.all
    }

    sealed class Media(
        storageType: com.anggrayudi.storage.media.MediaType,
        @StringRes labelRes: Int,
        @StringRes val fileDeclarationRes: Int,
        @DrawableRes iconRes: Int,
        color: Color,
        sourceKinds: List<SourceKind>
    ) : FileType(
        storageType = storageType,
        titleRes = labelRes,
        iconRes = iconRes,
        color = color,
        sourceKinds = sourceKinds
    ) {
        companion object {
            val all: List<Media> get() = listOf(Image, Video, Audio)
        }
    }

    @Parcelize
    object Image : Media(
        com.anggrayudi.storage.media.MediaType.IMAGE,
        R.string.image,
        R.string.image,
        R.drawable.ic_image_24,
        Color(0xFFBF1A2F),
        listOf(
            SourceKind.Camera,
            SourceKind.Screenshot,
            SourceKind.Download,
            SourceKind.ThirdPartyApp
        )
    )

    @Parcelize
    object Video : Media(
        com.anggrayudi.storage.media.MediaType.VIDEO,
        R.string.video,
        R.string.video,
        R.drawable.ic_video_file_24,
        Color(0xFFFFCB77),
        listOf(
            SourceKind.Camera,
            SourceKind.Download,
            SourceKind.ThirdPartyApp
        )
    )

    @Parcelize
    object Audio : Media(
        com.anggrayudi.storage.media.MediaType.AUDIO,
        R.string.audio,
        R.string.audio_file,
        R.drawable.ic_audio_file_24,
        Color(0xFFF26430),
        listOf(
            SourceKind.Download,
            SourceKind.ThirdPartyApp
        )
    )

    sealed class NonMedia(
        @StringRes labelRes: Int,
        @DrawableRes iconRes: Int,
        color: Color,
        val fileExtension: String
    ) : FileType(
        storageType = com.anggrayudi.storage.media.MediaType.DOWNLOADS,
        titleRes = labelRes,
        iconRes = iconRes,
        color = color,
        sourceKinds = listOf(
            SourceKind.Download
        )
    ) {
        companion object {
            val all: List<NonMedia> get() = listOf(PDF, Text, ZIP, APK)
        }
    }

    @Parcelize
    object PDF : NonMedia(
        R.string.pdf,
        R.drawable.ic_pdf_24,
        Color(0xFFD6BA73),
        "pdf"
    )

    @Parcelize
    object Text : NonMedia(
        R.string.text,
        R.drawable.ic_text_file_24,
        Color(0xFFF00699),
        "txt"
    )

    @Parcelize
    object ZIP : NonMedia(
        R.string.zip,
        R.drawable.ic_folder_zip_24,
        Color(0xFF826251),
        "zip"
    )

    @Parcelize
    object APK : NonMedia(
        R.string.apk,
        R.drawable.ic_apk_file_24,
        Color(0xFFFCB07E),
        "apk"
    )

    private val identifier = this::class.java.simpleName

    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(identifier)

    val sources: List<Source> = sourceKinds.map { Source(it, identifier) }

    val isMediaType: Boolean get() = this is Media
    val navigationRequiresManageExternalStoragePermission: Boolean get() = this is NonMedia

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
        ThirdPartyApp(
            R.string.third_party_app,
            R.drawable.ic_apps_24
        )
    }

    class Source(val kind: SourceKind, mediaTypeIdentifier: String) :
        DataStoreVariable<Boolean> {

        override val defaultValue: Boolean = true
        override val preferencesKey: Preferences.Key<Boolean> by lazy {
            booleanPreferencesKey("$mediaTypeIdentifier.$kind")
        }
    }
}