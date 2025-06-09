package com.w2sv.filenavigator.ui.modelext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarVisuals
import com.w2sv.core.common.R
import com.w2sv.domain.model.MovedFile
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import slimber.log.e

fun MovedFile.exists(context: Context): Boolean {
    return try {
        documentUri
            .documentFile(context)
            .exists() // prints "java.lang.IllegalArgumentException: Failed to determine if ... is child of ...: java.io.FileNotFoundException: Missing file for ... at ..." to console without actually raising it
    } catch (_: IllegalArgumentException) {
        false
    }
}

suspend fun MovedFile.launchViewMovedFileActivity(context: Context, showSnackbarOnError: suspend (SnackbarVisuals) -> Unit) {
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
        showSnackbarOnError(
            AppSnackbarVisuals(
                message = context.getString(
                    when (e) {
                        is ActivityNotFoundException -> R.string.provider_does_not_support_file_viewing
                        else -> R.string.can_t_view_file_from_within_file_navigator
                    }
                ),
                kind = SnackbarKind.Error
            )
        )
    }
}
