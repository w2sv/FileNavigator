package com.w2sv.filenavigator.mediastore

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.DataStoreVariable
import kotlinx.parcelize.Parcelize

sealed class FileType(
    val storageType: com.anggrayudi.storage.media.MediaType,
    @StringRes val labelRes: Int,
    @StringRes val fileLabelRes: Int,
    @DrawableRes val iconRes: Int,
    originKinds: List<OriginKind>
) : DataStoreVariable<Boolean>, Parcelable {

    companion object {
        val all: List<FileType> = Media.all + NonMedia.all
    }

    sealed class Media(
        storageType: com.anggrayudi.storage.media.MediaType,
        @StringRes labelRes: Int,
        @StringRes fileLabelRes: Int,
        @DrawableRes iconRes: Int,
        originKinds: List<OriginKind>
    ) : FileType(
        storageType = storageType,
        labelRes = labelRes,
        fileLabelRes = fileLabelRes,
        iconRes = iconRes,
        originKinds = originKinds
    ) {
        companion object {
            val all: List<Media> = listOf(Image, Video, Audio)
        }
    }

    @Parcelize
    object Image : Media(
        com.anggrayudi.storage.media.MediaType.IMAGE,
        R.string.image,
        R.string.image,
        R.drawable.ic_image_24,
        listOf(
            OriginKind.Camera,
            OriginKind.Screenshot,
            OriginKind.Download,
            OriginKind.ThirdPartyApp
        )
    )

    @Parcelize
    object Video : Media(
        com.anggrayudi.storage.media.MediaType.VIDEO,
        R.string.video,
        R.string.video,
        R.drawable.ic_video_file_24,
        listOf(
            OriginKind.Camera,
            OriginKind.Download,
            OriginKind.ThirdPartyApp
        )
    )

    @Parcelize
    object Audio : Media(
        com.anggrayudi.storage.media.MediaType.AUDIO,
        R.string.audio,
        R.string.audio_file,
        R.drawable.ic_audio_file_24,
        listOf(
            OriginKind.Download,
            OriginKind.ThirdPartyApp
        )
    )

    sealed class NonMedia(
        @StringRes labelRes: Int,
        @StringRes fileLabelRes: Int,
        @DrawableRes iconRes: Int
    ) : FileType(
        com.anggrayudi.storage.media.MediaType.DOWNLOADS,
        labelRes, fileLabelRes, iconRes, listOf(
            OriginKind.Download
        )
    ) {
        companion object {
            val all: List<NonMedia> = listOf(PDF, TXT, ZIP, APK)
        }
    }

    @Parcelize
    object PDF : NonMedia(
        R.string.pdf,
        R.string.pdf,
        R.drawable.ic_pdf_24
    )

    @Parcelize
    object TXT : NonMedia(
        R.string.txt,
        R.string.txt_file,
        R.drawable.ic_text_file_24
    )

    @Parcelize
    object ZIP : NonMedia(
        R.string.zip,
        R.string.zip_file,
        R.drawable.ic_folder_zip_24
    )

    @Parcelize
    object APK : NonMedia(
        R.string.apk,
        R.string.apk_file,
        R.drawable.ic_apk_file_24,
    )

    private val identifier = this::class.java.simpleName

    override val defaultValue: Boolean = true
    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(identifier)

    val origins: List<Origin> = originKinds.map { Origin(it, identifier) }

    enum class OriginKind(
        @StringRes val labelRes: Int
    ) {
        Camera(
            R.string.camera
        ),
        Screenshot(
            R.string.screenshot
        ),
        Download(
            R.string.download
        ),
        ThirdPartyApp(
            R.string.third_party_app
        )
    }

    class Origin(val kind: OriginKind, mediaTypeIdentifier: String) :
        DataStoreVariable<Boolean> {

        override val defaultValue: Boolean = true
        override val preferencesKey: Preferences.Key<Boolean> by lazy {
            booleanPreferencesKey("$mediaTypeIdentifier.$kind")
        }
    }
}