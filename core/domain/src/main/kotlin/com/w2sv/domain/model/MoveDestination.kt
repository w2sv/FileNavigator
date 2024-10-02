package com.w2sv.domain.model

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
import com.w2sv.common.utils.directoryName
import com.w2sv.core.domain.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed interface MoveDestination : Parcelable {
    val documentUri: DocumentUri
    fun uiRepresentation(context: Context): String
    fun fileName(context: Context): String

    @Parcelize
    @JvmInline
    value class Directory(override val documentUri: DocumentUri) : MoveDestination {

        fun hasReadAndWritePermission(context: Context): Boolean =
            documentUri.documentFile(context).name != null

        val isVolumeRoot: Boolean
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
                documentFile(context).directoryName(context)
            }

        fun pathRepresentation(context: Context, includeVolumeName: Boolean): String =
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
            fun parse(uriString: String): Directory =
                Directory(DocumentUri.parse(uriString))

            fun fromTreeUri(context: Context, treeUri: Uri): Directory? =
                DocumentUri.fromTreeUri(context, treeUri)?.let { Directory(it) }

            fun fromDocumentFile(documentFile: DocumentFile): Directory =
                Directory(DocumentUri(documentFile.uri))
        }
    }

    sealed interface File : MoveDestination {

        val localOrNull: Local?
            get() = this as? Local

        override fun fileName(context: Context): String =
            documentFile(context).name!!

        @Parcelize
        data class Local(override val documentUri: DocumentUri, val mediaUri: MediaUri) : File {

            @IgnoredOnParcel
            val parent: Directory by lazy {
                Directory(requireNotNull(documentUri.parent))
            }

            override fun uiRepresentation(context: Context): String =
                parent.uiRepresentation(context)
        }

        @Parcelize
        data class External(
            override val documentUri: DocumentUri,
            val providerPackageName: String?,
            val providerAppLabel: String?
        ) : File {

            companion object {
                fun get(documentUri: DocumentUri, context: Context): External {
                    return documentUri.uri.authority
                        ?.let {
                            context.packageManager.resolveContentProvider(
                                it,
                                PackageManager.GET_META_DATA
                            )
                        }
                        ?.let { providerInfo: ProviderInfo ->
                            External(
                                documentUri = documentUri,
                                providerPackageName = providerInfo.packageName,
                                providerAppLabel = context.packageManager.getApplicationLabel(
                                    providerInfo.applicationInfo
                                )
                                    .toString()
                            )
                        }
                        ?: External(
                            documentUri = documentUri,
                            providerPackageName = null,
                            providerAppLabel = null
                        )
                }
            }

            override fun uiRepresentation(context: Context): String {
                return providerAppLabel
                    ?: documentUri.uri.authority
                    ?: context.getString(R.string.unrecognized_destination)
            }
        }

        companion object {
            fun get(documentUri: DocumentUri, context: Context): File =  // TODO: test
                when (documentUri.uri.authority!!) {
                    "com.android.externalstorage.documents" -> Local(
                        documentUri = documentUri,
                        mediaUri = documentUri.mediaUri(context)!!
                    )

                    else -> External.get(
                        documentUri = documentUri,
                        context = context
                    )
                }
        }
    }

    val quickMoveDestination: Directory?
        get() = when (this) {
            is File.Local -> parent.let { if (it.isVolumeRoot) null else Directory(it.documentUri.documentTreeUri()) }
            is File.External -> null
            is Directory -> this
        }

    /**
     * @see DocumentFile.fromSingleUri
     */
    fun documentFile(context: Context): DocumentFile =
        documentUri.documentFile(context)
}