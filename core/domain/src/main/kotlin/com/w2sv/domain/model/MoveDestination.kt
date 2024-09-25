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

        override fun uiRepresentation(context: Context): String =
            "/${fileName(context)}"

        override fun fileName(context: Context): String =
            documentFile(context).directoryName(context)

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
            val parentDirectory: Directory by lazy {
                Directory(documentUri.parent!!)
            }

            override fun uiRepresentation(context: Context): String =
                parentDirectory.uiRepresentation(context)
        }

        @Parcelize
        @JvmInline
        value class Cloud(override val documentUri: DocumentUri) : File {

            override fun uiRepresentation(context: Context): String {
                return providerApplicationLabel(context)
                    ?: documentUri.uri.authority
                    ?: context.getString(R.string.unrecognized_destination)
            }

            /**
             * @return E.g. "Drive" for Google Drive, "Dropbox" for Dropbox.
             */
            fun providerApplicationLabel(context: Context): String? {
                return providerInfo(context)?.let { providerInfo ->
                    context.packageManager.getApplicationLabel(providerInfo.applicationInfo)
                        .toString()
                }
            }

            fun providerPackageName(context: Context): String? =
                providerInfo(context)?.packageName

            private fun providerInfo(context: Context): ProviderInfo? =
                documentUri.uri.authority?.let {
                    context.packageManager.resolveContentProvider(it, PackageManager.GET_META_DATA)
                }
        }

        companion object {
            fun get(documentUri: DocumentUri, context: Context): File =  // TODO: test
                when (documentUri.uri.authority!!) {
                    "com.android.externalstorage.documents" -> Local(
                        documentUri = documentUri,
                        mediaUri = documentUri.mediaUri(context)!!
                    )

                    else -> Cloud(
                        documentUri = documentUri
                    )
                }
        }
    }

    val quickMoveDestination: Directory?
        get() = when (this) {
            is File.Local -> Directory(parentDirectory.documentUri.documentTreeUri())
            is File.Cloud -> null
            is Directory -> this
        }

    /**
     * @see DocumentFile.fromSingleUri
     */
    fun documentFile(context: Context): DocumentFile =
        documentUri.documentFile(context)
}