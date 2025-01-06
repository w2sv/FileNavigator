package com.w2sv.domain.model.movedestination

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.w2sv.common.util.DocumentUri

interface MoveDestinationApi {
    val documentUri: DocumentUri
    fun fileName(context: Context): String

    fun uiRepresentation(context: Context): String

    /**
     * @see DocumentFile.fromSingleUri
     */
    fun documentFile(context: Context): DocumentFile =
        documentUri.documentFile(context)
}
