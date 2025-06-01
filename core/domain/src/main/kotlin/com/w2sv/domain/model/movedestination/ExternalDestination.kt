package com.w2sv.domain.model.movedestination

import android.content.Context
import com.w2sv.common.util.DocumentUri
import com.w2sv.core.common.R

data class ExternalDestination(
    override val documentUri: DocumentUri,
    override val providerPackageName: String?,
    override val providerAppLabel: String?
) : ExternalDestinationApi, FileDestinationApi {

    override fun uiRepresentation(context: Context): String =
        providerAppLabel
            ?: documentUri.uri.authority
            ?: context.getString(R.string.unrecognized_destination)
}
