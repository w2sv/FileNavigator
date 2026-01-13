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

fun MovedFile.startViewFileActivity(context: Context, onError: (Throwable) -> Unit) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .apply {
                    when (this@startViewFileActivity) {
                        is MovedFile.Local -> {
                            setDataAndType(
                                mediaUri?.uri ?: error("Media uri null"),
                                this@startViewFileActivity.fileType.mediaType.mimeType
                            )
                        }

                        is MovedFile.External -> {
                            setDataAndType(
                                documentUri.uri,
                                this@startViewFileActivity.fileType.mediaType.mimeType
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
