package com.w2sv.domain.model

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
import com.w2sv.common.utils.fileName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed interface MoveDestination : Parcelable {
    val documentUri: DocumentUri

    @Parcelize
    @JvmInline
    value class Directory(override val documentUri: DocumentUri) : Parcelable, MoveDestination {

        fun shortRepresentation(context: Context): String =
            "/${fileName(context)}"

        companion object {
            fun parse(uriString: String): Directory =
                Directory(DocumentUri.parse(uriString))

            fun fromTreeUri(context: Context, treeUri: Uri): Directory? =
                DocumentUri.fromTreeUri(context, treeUri)?.let { Directory(it) }

            fun fromDocumentFile(documentFile: DocumentFile): Directory =
                Directory(DocumentUri(documentFile.uri))
        }
    }

    @Parcelize
    data class File(override val documentUri: DocumentUri, val mediaUri: MediaUri) : Parcelable,
        MoveDestination {

        @IgnoredOnParcel
        val parentDirectory: Directory by lazy {
            Directory(documentUri.parent!!)
        }
    }

    val directoryDestination: Directory
        get() = when (this) {
            is File -> parentDirectory
            is Directory -> this
        }

    /**
     * @see DocumentFile.fromSingleUri
     */
    fun documentFile(context: Context): DocumentFile? =
        documentUri.documentFile(context)

    fun fileName(context: Context): String =
        documentFile(context)!!.fileName(context)
}