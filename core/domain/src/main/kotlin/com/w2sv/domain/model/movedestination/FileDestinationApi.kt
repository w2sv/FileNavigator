package com.w2sv.domain.model.movedestination

import android.content.Context

interface FileDestinationApi : MoveDestinationApi {
    override fun fileName(context: Context): String =
        documentFile(context).name!!  // TODO
}