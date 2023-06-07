package com.w2sv.filenavigator.mediastore

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.w2sv.filenavigator.R

data class MediaTypeOrigin(@StringRes val labelRes: Int)

enum class MediaType(
    val storageType: com.anggrayudi.storage.media.MediaType,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val origins: List<MediaTypeOrigin>
) {
    Image(
        com.anggrayudi.storage.media.MediaType.IMAGE,
        R.string.image,
        R.drawable.ic_image_24,
        listOf(
            MediaTypeOrigin(
                R.string.camera
            ),
            MediaTypeOrigin(
                R.string.screenshot
            ),
            MediaTypeOrigin(
                R.string.download
            ),
            MediaTypeOrigin(
                R.string.third_party_app
            )
        )
    ),
    Video(
        com.anggrayudi.storage.media.MediaType.VIDEO,
        R.string.video,
        R.drawable.ic_video_file_24,
        listOf(
            MediaTypeOrigin(
                R.string.camera
            ),
            MediaTypeOrigin(
                R.string.download
            ),
            MediaTypeOrigin(
                R.string.third_party_app
            )
        )
    ),
    Audio(
        com.anggrayudi.storage.media.MediaType.AUDIO,
        R.string.audio,
        R.drawable.ic_audio_file_24,
        listOf(
            MediaTypeOrigin(
                R.string.download
            ),
            MediaTypeOrigin(
                R.string.third_party_app
            )
        )
    ),
//    PDF(
//        com.anggrayudi.storage.media.MediaType.DOWNLOADS,
//        R.string.pdf,
//        R.drawable.ic_pdf_24
//    )
    ;

    val originIdentifiers: List<String> = origins.map { getOriginIdentifier(this, it) }
}

fun getOriginIdentifier(mediaType: MediaType, mediaTypeOrigin: MediaTypeOrigin): String =
    "${mediaType.name}.${mediaTypeOrigin.labelRes}"