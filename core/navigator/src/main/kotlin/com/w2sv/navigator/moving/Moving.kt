package com.w2sv.navigator.moving

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.result.SingleFileErrorCode
import com.anggrayudi.storage.result.SingleFileResult
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.kotlinutils.coroutines.firstBlocking
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import slimber.log.i

internal fun MoveBundle.move(context: Context): MoveResult {
    // Exit if 'manage all files' permission not granted
    if (!isExternalStorageManger) {
        return MoveResult.Failure.ManageAllFilesPermissionMissing
    }

    if (!file.mediaStoreData.fileExists) {
        return MoveResult.Failure.MoveFileNotFound
    }

    // Exit on unsuccessful conversion to SimpleStorage objects
    val moveDestinationDocumentFile = destination.documentFile(context)
    val moveMediaFile = file.simpleStorageMediaFile(context)

    if (moveDestinationDocumentFile == null || moveMediaFile == null) {
        return MoveResult.Failure.InternalError
    }

    // Exit if file already at destination
    if (moveDestinationDocumentFile.hasChild(  // TODO: optimizable?
            context = context,
            path = file.mediaStoreData.name,
            requiresWriteAccess = false
        )
    ) {
        return MoveResult.Failure.FileAlreadyAtDestination
    }

    return moveMediaFile.moveTo(
        targetFolder = moveDestinationDocumentFile,
        isFileSizeAllowed = { freeSpace, fileSize -> fileSize <= freeSpace },
        onConflict = onFileConflict
    )
        .map { moveState ->
            i { moveState.javaClass.name }

            when (moveState) {
                is SingleFileResult.Error -> {
                    i { moveState.errorCode.toString() }

                    when (moveState.errorCode) {
                        SingleFileErrorCode.TARGET_FOLDER_NOT_FOUND -> MoveResult.Failure.MoveDestinationNotFound(
                            this
                        )

                        SingleFileErrorCode.NO_SPACE_LEFT_ON_TARGET_PATH -> MoveResult.Failure.NotEnoughSpaceOnDestination
                        SingleFileErrorCode.SOURCE_FILE_NOT_FOUND -> MoveResult.Failure.MoveFileNotFound
                        SingleFileErrorCode.STORAGE_PERMISSION_DENIED, SingleFileErrorCode.CANNOT_CREATE_FILE_IN_TARGET -> MoveResult.Failure.ManageAllFilesPermissionMissing
                        else -> MoveResult.Failure.InternalError
                    }
                }

                is SingleFileResult.Completed -> {
                    MoveResult.Success(this)
                }

                else -> {
                    null
                }
            }
        }
        .filterNotNull()
        .firstBlocking()
}

private val onFileConflict = object : SingleFileConflictCallback<DocumentFile>() {}