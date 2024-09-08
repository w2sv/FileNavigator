package com.w2sv.navigator.moving

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.result.SingleFileErrorCode
import com.anggrayudi.storage.result.SingleFileResult
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.domain.model.MoveDestination
import com.w2sv.kotlinutils.coroutines.firstBlocking
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import slimber.log.e
import slimber.log.i

internal sealed interface PreMoveCheckResult {

    @JvmInline
    value class Success(val documentFile: DocumentFile) : PreMoveCheckResult

    abstract class Failure(val failure: MoveResult.Failure) : PreMoveCheckResult {

        data object ManageAllFilesPermissionMissing :
            Failure(MoveResult.Failure.ManageAllFilesPermissionMissing)

        data object InternalError : Failure(MoveResult.Failure.InternalError)
    }

    companion object {
        fun get(
            destination: MoveDestination,
            context: Context
        ): PreMoveCheckResult {
            val documentFile = destination.documentFile(context)  // TODO

            return when {
                !isExternalStorageManger -> Failure.ManageAllFilesPermissionMissing
                documentFile == null -> Failure.InternalError
                else -> Success(documentFile)
            }
        }
    }
}

internal fun MoveBundle.move(context: Context): MoveResult {
    val destinationDocumentFile = PreMoveCheckResult.get(destination, context).run {  // TODO
        when (this) {
            is PreMoveCheckResult.Success -> documentFile
            is PreMoveCheckResult.Failure -> return failure
        }
    }

    return file.moveTo(
        destination = destinationDocumentFile,
        context = context,
    )
}

internal fun MoveFile.moveTo(
    destination: DocumentFile,
    context: Context
): MoveResult {
    if (!mediaStoreFileData.fileExists) {
        return MoveResult.Failure.MoveFileNotFound
    }

    val mediaFile = simpleStorageMediaFile(context) ?: return MoveResult.Failure.InternalError

    // Exit if file already at destination
    if (destination.hasChild(  // TODO: optimizable?
            context = context,
            path = mediaStoreFileData.name,
            requiresWriteAccess = false
        )
    ) {
        return MoveResult.Failure.FileAlreadyAtDestination
    }

    return mediaFile.moveTo(
        targetFolder = destination,
        onConflict = onFileConflict
    )
        .map { moveState ->
            i { moveState.javaClass.name }

            when (moveState) {
                is SingleFileResult.Error -> {
                    i { moveState.errorCode.toString() }

                    when (moveState.errorCode) {
                        SingleFileErrorCode.TARGET_FOLDER_NOT_FOUND -> MoveResult.Failure.MoveDestinationNotFound
                        SingleFileErrorCode.NO_SPACE_LEFT_ON_TARGET_PATH -> MoveResult.Failure.NotEnoughSpaceOnDestination
                        SingleFileErrorCode.SOURCE_FILE_NOT_FOUND -> MoveResult.Failure.MoveFileNotFound
                        SingleFileErrorCode.STORAGE_PERMISSION_DENIED, SingleFileErrorCode.CANNOT_CREATE_FILE_IN_TARGET -> MoveResult.Failure.ManageAllFilesPermissionMissing
                        else -> MoveResult.Failure.InternalError
                    }
                }

                is SingleFileResult.Completed -> MoveResult.Success
                else -> null
            }
        }
        .filterNotNull()
        .firstBlocking()
}

// $$$$$$$$$$$$$$$$$$$$$$
// Copy and delete
// $$$$$$$$$$$$$$$$$$$$$$

internal fun MoveBundle.copyToDestinationAndDelete(context: Context): MoveResult {
    val destinationDocumentFile = PreMoveCheckResult.get(destination, context).run {  // TODO
        when (this) {
            is PreMoveCheckResult.Success -> documentFile
            is PreMoveCheckResult.Failure -> return failure
        }
    }

    return file.copyAndDelete(
        destination = destinationDocumentFile,
        context = context,
    )
}

internal fun MoveFile.copyAndDelete(destination: DocumentFile, context: Context): MoveResult {
    if (!mediaStoreFileData.fileExists) {
        return MoveResult.Failure.MoveFileNotFound
    }

    val mediaFile = simpleStorageMediaFile(context) ?: return MoveResult.Failure.InternalError

    return mediaFile.copyToFile(
        targetFile = destination,
        isEnoughSpace = null,
        deleteOnSuccess = true
    )
        .map { moveState ->
            i { moveState.javaClass.name }

            when (moveState) {
                is SingleFileResult.Error -> {
                    e { "${moveState.errorCode}: ${moveState.message}" }

//                    destination.delete().log { "Deleted destination file: $it" }

                    when (moveState.errorCode) {
                        SingleFileErrorCode.NO_SPACE_LEFT_ON_TARGET_PATH -> MoveResult.Failure.NotEnoughSpaceOnDestination
                        SingleFileErrorCode.SOURCE_FILE_NOT_FOUND -> MoveResult.Failure.MoveFileNotFound
                        SingleFileErrorCode.STORAGE_PERMISSION_DENIED -> MoveResult.Failure.ManageAllFilesPermissionMissing
                        else -> MoveResult.Failure.InternalError
                    }
                }

                is SingleFileResult.Completed -> MoveResult.Success

                else -> null
            }
        }
        .filterNotNull()
        .firstBlocking()
}

private val onFileConflict = object : SingleFileConflictCallback<DocumentFile>() {}