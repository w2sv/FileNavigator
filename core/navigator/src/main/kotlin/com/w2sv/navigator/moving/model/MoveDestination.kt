package com.w2sv.navigator.moving.model

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.MediaUri
import com.w2sv.domain.model.movedestination.ExternalDestination
import com.w2sv.domain.model.movedestination.ExternalDestinationApi
import com.w2sv.domain.model.movedestination.FileDestinationApi
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

internal sealed interface MoveDestination : Parcelable, MoveDestinationApi {

    val isFile get() = this is File
    val isExternal get() = this is ExternalDestinationApi

    @Parcelize
    @TypeParceler<LocalDestinationApi, LocalDestinationApiParceler>()
    data class Directory(private val localDestination: LocalDestinationApi) :
        MoveDestination,
        LocalDestinationApi by localDestination {

        constructor(documentUri: DocumentUri) : this(LocalDestination(documentUri))

        companion object {
            fun parse(uriString: String): Directory =
                Directory(DocumentUri.parse(uriString))

            fun fromTreeUri(context: Context, treeUri: Uri): Directory? =
                DocumentUri.fromTreeUri(context, treeUri)?.let { Directory(it) }
        }

        fun hasReadAndWritePermission(context: Context): Boolean =
            documentFile(context).name != null
    }

    sealed interface File : MoveDestination {

        val localOrNull: Local?
            get() = this as? Local

        @Parcelize
        data class Local(override val documentUri: DocumentUri, val mediaUri: MediaUri) :
            File,
            FileDestinationApi {

            @IgnoredOnParcel
            val parent: Directory by lazy {
                Directory(requireNotNull(documentUri.parent))
            }

            override fun uiRepresentation(context: Context): String =
                parent.uiRepresentation(context)
        }

        @Parcelize
        @TypeParceler<ExternalDestinationApi, ExternalDestinationApiParceler>()
        data class External(private val externalDestination: ExternalDestinationApi) :
            File,
            ExternalDestinationApi by externalDestination {

            constructor(
                documentUri: DocumentUri,
                providerPackageName: String?,
                providerAppLabel: String?
            ) : this(
                ExternalDestination(documentUri, providerPackageName, providerAppLabel)
            )

            companion object {
                operator fun invoke(documentUri: DocumentUri, context: Context): External =
                    documentUri.uri.authority
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

        companion object {
            operator fun invoke(documentUri: DocumentUri, context: Context): File =
                // TODO: test
                when (documentUri.uri.authority!!) {
                    "com.android.externalstorage.documents" -> Local(
                        documentUri = documentUri,
                        mediaUri = documentUri.mediaUri(context)!!
                    )

                    else -> External(
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
}

private object LocalDestinationApiParceler : Parceler<LocalDestinationApi> {

    override fun create(parcel: Parcel) =
        LocalDestination(
            ParcelCompat.readParcelable(
                parcel,
                DocumentUri::class.java.classLoader,
                DocumentUri::class.java
            )!!
        )

    override fun LocalDestinationApi.write(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(documentUri, flags)
    }
}

private object ExternalDestinationApiParceler : Parceler<ExternalDestinationApi> {

    override fun create(parcel: Parcel) =
        ExternalDestination(
            documentUri = ParcelCompat.readParcelable(
                parcel,
                DocumentUri::class.java.classLoader,
                DocumentUri::class.java
            )!!,
            providerPackageName = parcel.readString(),
            providerAppLabel = parcel.readString()
        )

    override fun ExternalDestinationApi.write(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(documentUri, flags)
        parcel.writeString(providerPackageName)
        parcel.writeString(providerAppLabel)
    }
}
