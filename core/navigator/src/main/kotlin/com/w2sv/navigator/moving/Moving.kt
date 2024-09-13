package com.w2sv.navigator.moving

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.result.SingleFileErrorCode
import com.anggrayudi.storage.result.SingleFileResult
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import slimber.log.e
import slimber.log.i

internal sealed interface PreCheckResult {

    @JvmInline
    value class Success(val documentFile: DocumentFile) : PreCheckResult

    abstract class Failure(val failure: MoveResult.Failure) : PreCheckResult {

        data object ManageAllFilesPermissionMissing :
            Failure(MoveResult.ManageAllFilesPermissionMissing)

        data object InternalError : Failure(MoveResult.InternalError)
    }

    companion object {
        fun get(
            destination: MoveDestination,
            context: Context
        ): PreCheckResult {
            val documentFile = destination.documentFile(context)  // TODO

            return when {
                !isExternalStorageManger -> Failure.ManageAllFilesPermissionMissing
                documentFile == null -> Failure.InternalError
                else -> Success(documentFile)
            }
        }
    }
}

internal suspend fun MoveFile.moveTo(
    destination: DocumentFile,
    context: Context,
    onResult: (MoveResult) -> Unit
) {
    if (!mediaStoreFileData.fileExists) {
        return onResult(MoveResult.MoveFileNotFound)
    }

    val mediaFile = simpleStorageMediaFile(context) ?: return onResult(MoveResult.InternalError)

    // Exit if file already at destination
    if (destination.hasChild(  // TODO: optimizable?
            context = context,
            path = mediaStoreFileData.name,
            requiresWriteAccess = false
        )
    ) {
        return onResult(MoveResult.FileAlreadyAtDestination)
    }

    mediaFile.moveTo(
        targetFolder = destination,
        onConflict = onFileConflict
    )
        .map { moveState ->
            i { moveState.javaClass.name }

            when (moveState) {
                is SingleFileResult.Error -> {
                    e { "${moveState.errorCode}: ${moveState.message}" }

                    when (moveState.errorCode) {
                        SingleFileErrorCode.TARGET_FOLDER_NOT_FOUND -> MoveResult.MoveDestinationNotFound
                        SingleFileErrorCode.NO_SPACE_LEFT_ON_TARGET_PATH -> MoveResult.NotEnoughSpaceOnDestination
                        SingleFileErrorCode.SOURCE_FILE_NOT_FOUND -> MoveResult.MoveFileNotFound
                        SingleFileErrorCode.STORAGE_PERMISSION_DENIED, SingleFileErrorCode.CANNOT_CREATE_FILE_IN_TARGET -> MoveResult.ManageAllFilesPermissionMissing
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

// $$$$$$$$$$$$$$$$$$$$$$
// Copy and delete
// $$$$$$$$$$$$$$$$$$$$$$

internal suspend fun MoveBundle.copyToDestinationAndDelete(
    context: Context,
    onResult: (MoveResult) -> Unit
) {
    val destinationDocumentFile = PreCheckResult.get(destination, context).let { preCheckResult ->
        when (preCheckResult) {
            is PreCheckResult.Success -> preCheckResult.documentFile
            is PreCheckResult.Failure -> return onResult(preCheckResult.failure)
        }
    }

    file.copyAndDelete(
        destination = destinationDocumentFile,
        context = context,
        onResult = onResult
    )
}

private suspend fun MoveFile.copyAndDelete(
    destination: DocumentFile,
    context: Context,
    onResult: (MoveResult) -> Unit
) {
    if (!mediaStoreFileData.fileExists) {  // TODO
        return onResult(MoveResult.MoveFileNotFound)
    }

    val mediaFile = simpleStorageMediaFile(context) ?: return onResult(MoveResult.InternalError)

    mediaFile.copyToFile(
        targetFile = destination,
        deleteOnSuccess = true
    )
        .map { moveState ->
            i { moveState.javaClass.name }

            when (moveState) {
                is SingleFileResult.Error -> {
                    e { "${moveState.errorCode}: ${moveState.message}" }

//                    destination.delete().log { "Deleted destination file: $it" }

                    when (moveState.errorCode) {
                        SingleFileErrorCode.NO_SPACE_LEFT_ON_TARGET_PATH -> MoveResult.NotEnoughSpaceOnDestination
                        SingleFileErrorCode.SOURCE_FILE_NOT_FOUND -> MoveResult.MoveFileNotFound
                        SingleFileErrorCode.STORAGE_PERMISSION_DENIED -> MoveResult.ManageAllFilesPermissionMissing
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