package com.w2sv.filenavigator.mediastore

import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.w2sv.filenavigator.R

enum class MediaType(
    val mediaStoreUri: Uri,
    val simpleStorageMediaType: com.anggrayudi.storage.media.MediaType,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    Image(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.IMAGE,
        R.string.image,
        R.drawable.ic_image_24
    ),
    Video(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.VIDEO,
        R.string.video,
        R.drawable.ic_video_file_24
    ),
    Audio(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.AUDIO,
        R.string.audio,
        R.drawable.ic_audio_file_24
    ),
    PDF(
        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.DOWNLOADS,
        R.string.pdf,
        R.drawable.ic_pdf_24
    )
}