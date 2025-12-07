package com.w2sv.database.typeconverter

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter

internal object UriConverter {

    @TypeConverter
    fun fromUri(uri: Uri?): String =
        uri?.toString() ?: ""

    @TypeConverter
    fun toUri(uriString: String): Uri? =
        if (uriString.isNotEmpty()) uriString.toUri() else null
}
