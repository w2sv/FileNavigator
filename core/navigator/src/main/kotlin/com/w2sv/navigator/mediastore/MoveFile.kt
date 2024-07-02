package com.w2sv.navigator.mediastore

import android.os.Parcelable
import com.w2sv.common.utils.MediaUri
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MoveFile(
    val mediaUri: MediaUri,
    val mediaStoreData: MediaStoreData,
    val contentHash: String
) : Parcelable