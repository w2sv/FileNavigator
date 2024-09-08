package com.w2sv.domain.model

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.fileName
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class MoveDestination(val documentUri: DocumentUri) : Parcelable {

    fun documentFile(context: Context): DocumentFile? =
        documentUri.documentFile(context)

    fun shortRepresentation(context: Context): String =
        "/${documentFile(context)!!.fileName(context)}"

    override fun toString(): String =
        documentUri.toString()

    companion object {
        fun parse(uriString: String): MoveDestination =
            MoveDestination(DocumentUri.parse(uriString))

        fun fromTreeUri(context: Context, treeUri: Uri): MoveDestination? =
            DocumentUri.fromTreeUri(context, treeUri)?.let { MoveDestination(it) }

        fun fromDocumentFile(documentFile: DocumentFile): MoveDestination =
            MoveDestination(DocumentUri(documentFile.uri))
    }
}