package com.w2sv.filenavigator.ui.modelext

import android.content.Context
import android.content.Intent
import com.w2sv.domain.model.MoveEntry

fun MoveEntry.movedFileExists(context: Context): Boolean {
    return try {
        movedFileDocumentUri
            .documentFile(context)
            .exists()  // prints "java.lang.IllegalArgumentException: Failed to determine if ... is child of ...: java.io.FileNotFoundException: Missing file for ... at ..." to console without actually raising it
    } catch (e: IllegalArgumentException) {
        false
    }
}

fun MoveEntry.launchViewActivity(context: Context) {
    movedFileMediaUri?.let {  // TODO
        context.startActivity(
            Intent()
                .setAction(Intent.ACTION_VIEW)
                .setDataAndType(
                    it.uri,
                    fileType.simpleStorageMediaType.mimeType
                )
        )
    }
}