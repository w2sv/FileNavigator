package com.w2sv.navigator.moving.model

import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.utils.DocumentUri
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MoveBundle(
    val file: MoveFile,
    val destination: DocumentUri,
    val mode: MoveMode
) : Parcelable {

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.MoveBundle"

        fun fromIntent(intent: Intent): MoveBundle =
            intent.getParcelableCompat<MoveBundle>(EXTRA)!!
    }
}