package com.w2sv.navigator.moving.model

import android.os.Parcelable
import com.w2sv.domain.model.MoveDestination
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class BatchMoveBundle(val moveFile: MoveFile, val moveMode: MoveMode.Batchable) :
    Parcelable {

    fun moveBundle(destination: MoveDestination): MoveBundle =
        MoveBundle(
            file = moveFile,
            destination = destination,
            mode = moveMode
        )
}