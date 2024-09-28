package com.w2sv.database.entity

import android.net.Uri

internal sealed interface MoveDestinationEntryEntity {
    val destination: Uri

    data class Local(
        override val destination: Uri,
        val movedFileDocumentUri: Uri,
        val movedFileMediaUri: Uri
    ) : MoveDestinationEntryEntity

    data class Cloud(override val destination: Uri) : MoveDestinationEntryEntity
}