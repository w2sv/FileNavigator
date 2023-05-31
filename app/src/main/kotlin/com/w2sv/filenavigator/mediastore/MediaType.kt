package com.w2sv.filenavigator.mediastore

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.w2sv.filenavigator.R

enum class MediaType(
    val storageType: com.anggrayudi.storage.media.MediaType,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    Image(
        com.anggrayudi.storage.media.MediaType.IMAGE,
        R.string.image,
        R.drawable.ic_image_24
    ),
    Video(
        com.anggrayudi.storage.media.MediaType.VIDEO,
        R.string.video,
        R.drawable.ic_video_file_24
    ),
    Audio(
        com.anggrayudi.storage.media.MediaType.AUDIO,
        R.string.audio,
        R.drawable.ic_audio_file_24
    ),
    PDF(
        com.anggrayudi.storage.media.MediaType.DOWNLOADS,
        R.string.pdf,
        R.drawable.ic_pdf_24
    )
}