package com.w2sv.domain.model.movedestination

import android.content.Context

interface LocalDestinationApi : MoveDestinationApi {
    val isVolumeRoot: Boolean

    fun pathRepresentation(context: Context, includeVolumeName: Boolean): String
}