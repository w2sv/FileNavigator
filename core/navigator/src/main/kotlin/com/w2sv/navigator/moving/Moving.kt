package com.w2sv.navigator.moving

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.result.SingleFileError
import com.anggrayudi.storage.result.SingleFileResult
import com.w2sv.common.logging.log
import com.w2sv.common.util.hasChild
import com.w2sv.common.util.hasManageAllFilesPermission
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveFile
import com.w2sv.navigator.domain.moving.MoveResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import slimber.log.e
import slimber.log.i

internal suspend fun MoveFile.moveTo(
    context: Context,
    destination: MoveDestination,
    destinationDocumentFile: DocumentFile = destination.documentFile(context),
    onResult: (MoveResult) -> Unit
) {
    val mediaFile = mediaFile(context)

    when {
        !mediaStoreData.fileExists -> onResult(MoveResult.MoveFileNotFound)
        !hasManageAllFilesPermission -> onResult(MoveResult.ManageAllFilesPermissionMissing)
        mediaFile == null -> onResult(MoveResult.InternalError)

        destination.isFile -> mediaFile.copyToFileDestinationAndDelete(
            fileDestination = destinationDocumentFile,
            isExternalDestination = destination.isExternal,
            onResult = onResult
        )

        destinationDocumentFile.hasChild(context = context, path = mediaStoreData.name) -> onResult(MoveResult.FileAlreadyAtDestination)

        destination.isDirectory -> mediaFile.moveToFolder(
            folderDestination = destinationDocumentFile,
            onResult = onResult
        )
    }
}

/**
 * Attempts to move this [MediaFile] to the [folderDestination] and eventually invokes [onResult].
 *
 * This moving method is used for
 * - quick move
 * - auto move
 * - batch move
 */
private suspend fun MediaFile.moveToFolder(folderDestination: DocumentFile, onResult: (MoveResult) -> Unit) {
    moveTo(targetFolder = folderDestination, onConflict = onFileConflict)
        .map { moveState ->
            log(moveState)

            when (moveState) {
                is SingleFileResult.Error -> {
                    when (moveState.errorCode) {
                        is SingleFileError.TargetNotFound -> MoveResult.MoveDestinationNotFound
                        is SingleFileError.NotEnoughSpaceOnTarget -> MoveResult.NotEnoughSpaceOnDestination
                        is SingleFileError.SourceNotFound -> MoveResult.MoveFileNotFound
                        is SingleFileError.StoragePermissionMissing -> MoveResult.ManageAllFilesPermissionMissing
                        else -> MoveResult.InternalError
                    }
                }

                is SingleFileResult.Completed -> MoveResult.Success
                else -> null
            }
        }
        .filterNotNull()
        .collect(onResult)
}

private suspend fun MediaFile.copyToFileDestinationAndDelete(
    fileDestination: DocumentFile,
    isExternalDestination: Boolean,
    onResult: (MoveResult) -> Unit
) {
    i { "Destination writable: ${fileDestination.canWrite()}" }

    copyToFile(
        targetFile = fileDestination,
        deleteOnSuccess = true,
        checkIfEnoughSpaceOnTarget = !isExternalDestination,
        checkIfTargetWritable = !isExternalDestination
    )
        .map { moveState ->
            log(moveState)

            when (moveState) {
                is SingleFileResult.Error -> {
                    // Try to delete the now pointless destination file
                    try {
                        fileDestination.delete().log { "Deleted destination file: $it" }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    when (moveState.errorCode) {
                        is SingleFileError.NotEnoughSpaceOnTarget -> MoveResult.NotEnoughSpaceOnDestination
                        SingleFileError.SourceNotFound -> MoveResult.MoveFileNotFound
                        is SingleFileError.StoragePermissionMissing -> MoveResult.ManageAllFilesPermissionMissing
                        else -> MoveResult.InternalError
                    }
                }

                is SingleFileResult.Completed -> MoveResult.Success

                else -> null
            }
        }
        .filterNotNull()
        .collect(onResult)
}

private val onFileConflict = object : SingleFileConflictCallback<DocumentFile>() {}

private fun log(result: SingleFileResult) {
    when (result) {
        is SingleFileResult.Error -> e {
            buildString {
                append("${result.javaClass.simpleName}: ${result.errorCode}")
                result.message?.let { append(" - $it") }
            }
        }

        else -> i { "MoveResult: ${result.javaClass.simpleName}" }
    }
}
