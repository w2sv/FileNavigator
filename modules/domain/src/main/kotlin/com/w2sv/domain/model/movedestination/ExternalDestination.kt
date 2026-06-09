package com.w2sv.domain.model.movedestination

import android.content.Context
import com.w2sv.modules.resources.R
import com.w2sv.storage.uri.DocumentUri

data class ExternalDestination(
    override val documentUri: DocumentUri,
    override val providerPackageName: String?,
    override val providerAppLabel: String?
) : ExternalDestinationApi,
    FileDestinationApi {

    override fun uiRepresentation(context: Context): String =
        providerAppLabel
            ?: documentUri.uri.authority
            ?: context.getString(R.string.unrecognized_destination)
}
