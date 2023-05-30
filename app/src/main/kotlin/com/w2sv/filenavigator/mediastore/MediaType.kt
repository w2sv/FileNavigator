package com.w2sv.filenavigator.mediastore

import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import com.w2sv.filenavigator.R

enum class MediaType(
    val mediaStoreUri: Uri,
    val simpleStorageMediaType: com.anggrayudi.storage.media.MediaType,
    @DrawableRes val iconRes: Int
) {
    Image(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.IMAGE,
        R.drawable.ic_image_24
    ),
    Video(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.VIDEO,
        R.drawable.ic_video_file_24
    ),
    Audio(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.AUDIO,
        R.drawable.ic_audio_file_24
    )
}