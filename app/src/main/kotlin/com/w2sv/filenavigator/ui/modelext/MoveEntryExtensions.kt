package com.w2sv.filenavigator.ui.modelext

import android.content.Context
import android.content.Intent
import com.w2sv.domain.model.MovedFile
import slimber.log.e

fun MovedFile.exists(context: Context): Boolean =
    try {
        // prints "java.lang.IllegalArgumentException: Failed to determine if ... is child of ...:
        // java.io.FileNotFoundException: Missing file for ... at ..." to console without actually raising it
        documentUri
            .documentFile(context)
            .exists()
    } catch (_: IllegalArgumentException) {
        false
    }

suspend fun MovedFile.launchViewMovedFileActivity(context: Context, onError: suspend (Throwable) -> Unit) {
    try {
        context.startActivity(
            Intent()
                .setAction(Intent.ACTION_VIEW)
                .apply {
                    when (this@launchViewMovedFileActivity) {
                        is MovedFile.Local -> {
                            setDataAndType(
                                mediaUri?.uri ?: run { throw IllegalArgumentException("Media uri null") },
                                this@launchViewMovedFileActivity.fileType.mediaType.mimeType
                            )
                        }

                        is MovedFile.External -> {
                            setDataAndType(
                                documentUri.uri,
                                this@launchViewMovedFileActivity.fileType.mediaType.mimeType
                            )
                            setPackage(moveDestination.providerPackageName)
                        }
                    }
                }
        )
    } catch (e: Throwable) {
        e { e.toString() }
        onError(e)
    }
}
