package com.w2sv.filenavigator.mediastore

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.DataStoreVariable

enum class MediaType(
    val storageType: com.anggrayudi.storage.media.MediaType,
    @StringRes val labelRes: Int,
    @StringRes val fileLabelRes: Int,
    @DrawableRes val iconRes: Int,
    val isCoreType: Boolean,
    originKinds: List<OriginKind>
) : DataStoreVariable<Boolean> {

    Image(
        com.anggrayudi.storage.media.MediaType.IMAGE,
        R.string.image,
        R.string.image,
        R.drawable.ic_image_24,
        true,
        listOf(
            OriginKind.Camera,
            OriginKind.Screenshot,
            OriginKind.Download,
            OriginKind.ThirdPartyApp
        )
    ),
    Video(
        com.anggrayudi.storage.media.MediaType.VIDEO,
        R.string.video,
        R.string.video,
        R.drawable.ic_video_file_24,
        true,
        listOf(
            OriginKind.Camera,
            OriginKind.Download,
            OriginKind.ThirdPartyApp
        )
    ),
    Audio(
        com.anggrayudi.storage.media.MediaType.AUDIO,
        R.string.audio,
        R.string.audio_file,
        R.drawable.ic_audio_file_24,
        true,
        listOf(
            OriginKind.Download,
            OriginKind.ThirdPartyApp
        )
    ),
    PDF(
        com.anggrayudi.storage.media.MediaType.DOWNLOADS,
        R.string.pdf,
        R.string.pdf,
        R.drawable.ic_pdf_24,
        false,
        listOf(
            OriginKind.Download
        )
    ),
    TXT(
        com.anggrayudi.storage.media.MediaType.DOWNLOADS,
        R.string.txt,
        R.string.txt_file,
        R.drawable.ic_text_file_24,
        false,
        listOf(
            OriginKind.Download
        )
    ),
    ZIP(
        com.anggrayudi.storage.media.MediaType.DOWNLOADS,
        R.string.zip,
        R.string.zip_file,
        R.drawable.ic_folder_zip_24,
        false,
        listOf(
            OriginKind.Download
        )
    ),
    APK(
        com.anggrayudi.storage.media.MediaType.DOWNLOADS,
        R.string.apk,
        R.string.apk_file,
        R.drawable.ic_apk_file_24,
        false,
        listOf(
            OriginKind.Download
        )
    );

    override val defaultValue: Boolean = true
    override val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey(name)

    val origins: List<Origin> = originKinds.map { Origin(it, name) }

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