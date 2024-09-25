package com.w2sv.filenavigator.ui.modelext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.w2sv.domain.model.MoveDestination
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

fun MoveEntry.launchViewMovedFileActivity(context: Context) {
    try {
        context.startActivity(
            Intent()
                .setAction(Intent.ACTION_VIEW)
                .apply {
                    when (val capturedDestination = destination) {
                        is MoveDestination.Directory -> {
                            setDataAndType(
                                movedFileMediaUri!!.uri,
                                fileType.simpleStorageMediaType.mimeType
                            )
                        }

                        is MoveDestination.File.Cloud -> {
                            setDataAndType(
                                movedFileDocumentUri.uri,
                                fileType.simpleStorageMediaType.mimeType
                            )
                            setPackage(capturedDestination.providerPackageName(context))
                        }

                        else -> throw IllegalArgumentException()
                    }
                }
        )
    } catch (e: ActivityNotFoundException) {

    }
}