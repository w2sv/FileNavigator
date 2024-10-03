package com.w2sv.filenavigator.ui.modelext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarVisuals
import com.w2sv.domain.model.MovedFile
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind

fun MovedFile.exists(context: Context): Boolean {
    return try {
        documentUri
            .documentFile(context)
            .exists()  // prints "java.lang.IllegalArgumentException: Failed to determine if ... is child of ...: java.io.FileNotFoundException: Missing file for ... at ..." to console without actually raising it
    } catch (e: IllegalArgumentException) {
        false
    }
}

fun MovedFile.launchViewMovedFileActivity(context: Context): SnackbarVisuals? {
    return try {
        context.startActivity(
            Intent()
                .setAction(Intent.ACTION_VIEW)
                .apply {
                    localOrNull?.let {
                        setDataAndType(
                            it.mediaUri.uri,
                            this@launchViewMovedFileActivity.type.simpleStorageMediaType.mimeType
                        )
                    }
                    externalOrNull?.let {
                        setDataAndType(
                            documentUri.uri,
                            this@launchViewMovedFileActivity.type.simpleStorageMediaType.mimeType
                        )
                        setPackage(it.moveDestination.providerPackageName)
                    }
                }
        )
        null
    } catch (e: ActivityNotFoundException) {
        AppSnackbarVisuals(
            message = context.getString(R.string.provider_does_not_support_file_viewing),
            kind = SnackbarKind.Error
        )
    }
}