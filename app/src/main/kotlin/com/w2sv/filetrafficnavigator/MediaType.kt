package com.w2sv.filetrafficnavigator

import android.net.Uri
import android.provider.MediaStore

enum class MediaType(val mediaStoreUri: Uri) {
    Images(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
    Videos(MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
    Audio(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
}