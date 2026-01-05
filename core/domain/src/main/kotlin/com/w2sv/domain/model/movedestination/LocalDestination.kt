package com.w2sv.domain.model.movedestination

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.common.uri.DocumentUri
import com.w2sv.core.common.R

@JvmInline
value class LocalDestination(override val documentUri: DocumentUri) : LocalDestinationApi {

    override val isVolumeRoot: Boolean
        get() = documentUri.isVolumeRoot

    override fun uiRepresentation(context: Context): String =
        buildString {
            if (!isVolumeRoot) {
                append("/")
            }
            append(fileName(context))
        }

    override fun fileName(context: Context): String =
        if (isVolumeRoot) {
            context.getString(R.string.volume_root)
        } else {
            documentUri.documentFile(context).directoryName(context)
        }

    override fun pathRepresentation(context: Context, includeVolumeName: Boolean): String =
        if (isVolumeRoot) {
            if (includeVolumeName) {
                documentUri.volumeName
            } else {
                context.getString(R.string.volume_root)
            }
        } else {
            documentUri.documentFilePath(context)
                .let { path ->
                    if (includeVolumeName) {
                        path
                    } else {
                        "/${path.substringAfter(":")}"
                    }
                }
        }

    companion object {
        fun parse(uriString: String): LocalDestination =
            LocalDestination(DocumentUri.parse(uriString))

        fun fromTreeUri(context: Context, treeUri: Uri): LocalDestination? =
            DocumentUri.fromTreeUri(context, treeUri)?.let { LocalDestination(it) }
    }
}

private fun DocumentFile.directoryName(context: Context): String =
    getSimplePath(context).let { simplePath ->
        val substringAfterLastSlash = simplePath.substringAfterLast("/")
        if (substringAfterLastSlash != simplePath) {
            substringAfterLastSlash
        } else {
            simplePath.substringAfterLast(":")
        }
    }
