package com.w2sv.navigator.moving

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.result.SingleFileError
import com.anggrayudi.storage.result.SingleFileResult
import com.w2sv.common.util.hasChild
import com.w2sv.common.util.isExternalStorageManger
import com.w2sv.common.util.log
import com.w2sv.navigator.moving.model.MoveDestination
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import slimber.log.e
import slimber.log.i

internal sealed interface PreCheckResult {

    @JvmInline
    value class Success(val moveMediaFile: MediaFile) : PreCheckResult

    @JvmInline
    value class Failure(val reason: MoveResult.Failure) : PreCheckResult

    companion object {
        operator fun invoke( // TODO: test
            moveFile: MoveFile,
            context: Context,
            destinationDirectory: DocumentFile?
        ): PreCheckResult {
            val moveMediaFile = moveFile.simpleStorageMediaFile(context)

            return when {
                !moveFile.mediaStoreFileData.fileExists -> Failure(MoveResult.MoveFileNotFound)
                !isExternalStorageManger -> Failure(MoveResult.ManageAllFilesPermissionMissing)
                moveMediaFile == null -> Failure(MoveResult.InternalError)
                destinationDirectory?.hasChild(
                    context = context,
                    path = moveFile.mediaStoreFileData.name,
                    requiresWriteAccess = false
                ) == true -> Failure(MoveResult.FileAlreadyAtDestination)

                else -> Success(moveMediaFile = moveMediaFile)
            }
        }
    }
}

internal suspend fun MoveFile.moveTo(
    destination: MoveDestination,
    context: Context,
    destinationDocumentFile: DocumentFile = destination.documentFile(context),
    onResult: (MoveResult) -> Unit
) {
    when (
        val preCheckResult = PreCheckResult(
            moveFile = this,
            context = context,
            destinationDirectory = if (destination.isFile) null else destinationDocumentFile
        )
    ) {
        is PreCheckResult.Success -> {
            when (destination) {
                is MoveDestination.File -> {
                    preCheckResult.moveMediaFile.copyToFileDestinationAndDelete(
                        fileDestination = destinationDocumentFile,
                        isExternalDestination = destination.isExternal,
                        onResult = onResult
                    )
                }

                is MoveDestination.Directory -> {
                    preCheckResult.moveMediaFile.moveTo(
                        folderDestination = destinationDocumentFile,
                        onResult = onResult
                    )
                }
            }
        }

        is PreCheckResult.Failure -> {
            onResult(preCheckResult.reason)
        }
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
internal suspend fun MediaFile.moveTo(folderDestination: DocumentFile, onResult: (MoveResult) -> Unit) {
    moveTo(
        targetFolder = folderDestination,
        onConflict = onFileConflict
    )
        .map { moveState ->
            moveState.logTypeSpecifically()

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
            moveState.logTypeSpecifically()

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

private fun SingleFileResult.logTypeSpecifically() {
    when (this) {
        is SingleFileResult.Error -> e { debugString }
        else -> i { "MoveResult: ${javaClass.simpleName}" }
    }
}

private val SingleFileResult.Error.debugString: String
    get() = buildString {
        append("${this@debugString.javaClass.simpleName}: $errorCode")
        message?.let {
            append(" - $it")
        }
    }
