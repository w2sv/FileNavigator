package com.w2sv.navigator.model

import android.content.Context
import com.w2sv.data.model.FileType

fun FileType.Source.getTitle(context: Context): String =
    when (kind) {
        FileType.Source.Kind.Screenshot -> "Screenshot"
        FileType.Source.Kind.Camera -> {
            if (fileType == FileType.Media.Image)
                "Photo"
            else
                "Video"
        }

        FileType.Source.Kind.Download -> "${context.getString(fileType.titleRes)} Download"
        FileType.Source.Kind.OtherApp -> "External App ${context.getString(fileType.titleRes)}"
    }