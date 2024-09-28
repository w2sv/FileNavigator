package com.w2sv.filenavigator.ui.modelext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarVisuals
import com.w2sv.domain.model.AnyMoveDestinationEntry
import com.w2sv.domain.model.MoveEntry
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarAction
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind

fun AnyMoveDestinationEntry.movedFileExists(context: Context): Boolean {
    return try {
        movedFileDocumentUri
            .documentFile(context)
            .exists()  // prints "java.lang.IllegalArgumentException: Failed to determine if ... is child of ...: java.io.FileNotFoundException: Missing file for ... at ..." to console without actually raising it
    } catch (e: IllegalArgumentException) {
        false
    }
}

fun MoveEntry.launchViewMovedFileActivity(context: Context): SnackbarVisuals? {
    return try {
        context.startActivity(
            Intent()
                .setAction(Intent.ACTION_VIEW)
                .apply {
                    destinationEntry.localOrNull?.let {
                        setDataAndType(
                            it.movedFileMediaUri.uri,
                            fileType.simpleStorageMediaType.mimeType
                        )
                    }
                    destinationEntry.externalOrNull?.let {
                        setDataAndType(
                            it.movedFileDocumentUri.uri,
                            fileType.simpleStorageMediaType.mimeType
                        )
                        setPackage(it.destination.providerPackageName)
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