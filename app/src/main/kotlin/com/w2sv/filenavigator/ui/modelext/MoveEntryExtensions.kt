package com.w2sv.filenavigator.ui.modelext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarVisuals
import com.w2sv.core.common.R
import com.w2sv.domain.model.MovedFile
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind

fun MovedFile.exists(context: Context): Boolean {
    return try {
        documentUri
            .documentFile(context)
            .exists() // prints "java.lang.IllegalArgumentException: Failed to determine if ... is child of ...: java.io.FileNotFoundException: Missing file for ... at ..." to console without actually raising it
    } catch (_: IllegalArgumentException) {
        false
    }
}

fun MovedFile.launchViewMovedFileActivity(context: Context): SnackbarVisuals? {
    return try {
        context.startActivity(
            Intent()
                .setAction(Intent.ACTION_VIEW)
                .apply {
                    when (this@launchViewMovedFileActivity) {
                        is MovedFile.Local -> {
                            setDataAndType(
                                mediaUri.uri,
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
        null
    } catch (_: ActivityNotFoundException) {
        AppSnackbarVisuals(
            message = context.getString(R.string.provider_does_not_support_file_viewing),
            kind = SnackbarKind.Error
        )
    }
}
