package com.w2sv.filenavigator.mediastore

import android.net.Uri
import android.provider.MediaStore

enum class MediaType(
    val mediaStoreUri: Uri,
    val simpleStorageMediaType: com.anggrayudi.storage.media.MediaType
) {
    Image(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.IMAGE
    ),
    Video(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.VIDEO
    ),
    Audio(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        com.anggrayudi.storage.media.MediaType.AUDIO
    )
}