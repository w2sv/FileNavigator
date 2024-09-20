package com.w2sv.navigator.moving

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.result.SingleFileErrorCode
import com.anggrayudi.storage.result.SingleFileResult
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.domain.model.MoveDestination
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
        /**
         * TODO: test
         */
        fun get(
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
    destinationDocumentFile: DocumentFile = destination.documentFile(context)!!,  // TODO
    onResult: (MoveResult) -> Unit
) {
    when (val preCheckResult = PreCheckResult.get(
        moveFile = this,
        context = context,
        destinationDirectory = if (destination is MoveDestination.File) null else destinationDocumentFile
    )) {
        is PreCheckResult.Success -> {
            when (destination) {
                is MoveDestination.File -> {
                    preCheckResult.moveMediaFile.copyToFileDestinationAndDelete(
                        fileDestination = destinationDocumentFile,
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

internal suspend fun MediaFile.moveTo(
    folderDestination: DocumentFile,
    onResult: (MoveResult) -> Unit
) {
    moveTo(
        targetFolder = folderDestination,
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

private suspend fun MediaFile.copyToFileDestinationAndDelete(
    fileDestination: DocumentFile,
    onResult: (MoveResult) -> Unit
) {
    copyToFile(
        targetFile = fileDestination,
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