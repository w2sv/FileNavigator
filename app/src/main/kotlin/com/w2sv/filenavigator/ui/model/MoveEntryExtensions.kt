package com.w2sv.filenavigator.ui.model

import android.content.Context
import android.content.Intent
import androidx.documentfile.provider.DocumentFile
import com.w2sv.domain.model.MoveEntry

fun MoveEntry.movedFileExists(context: Context): Boolean {
    return try {
        DocumentFile.fromSingleUri(
            context,
            movedFileUri
        )
            ?.exists() == true  // prints "java.lang.IllegalArgumentException: Failed to determine if ... is child of ...: java.io.FileNotFoundException: Missing file for ... at ..." to console without actually raising it
    } catch (e: IllegalArgumentException) {
        false
    }
}

fun MoveEntry.launchViewActivity(context: Context) {
    context.startActivity(
        Intent()
            .setAction(Intent.ACTION_VIEW)
            .setDataAndType(
                movedFileMediaUri,
                fileType.simpleStorageMediaType.mimeType
            )
    )
}