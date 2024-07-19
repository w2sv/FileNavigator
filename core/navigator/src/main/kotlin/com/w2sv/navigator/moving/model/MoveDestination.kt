package com.w2sv.navigator.moving.model

import android.content.Context
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.fileName
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
internal value class MoveDestination(val documentUri: DocumentUri) : Parcelable {

    fun documentFile(context: Context): DocumentFile? =
        documentUri.documentFile(context)

    fun shortRepresentation(context: Context): String =
        "/${documentFile(context)!!.fileName(context)}"
}