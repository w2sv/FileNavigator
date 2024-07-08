package com.w2sv.navigator.moving

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.result.SingleFileErrorCode
import com.anggrayudi.storage.result.SingleFileResult
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.kotlinutils.coroutines.firstBlocking
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FileMover @Inject constructor() {

    operator fun invoke(
        moveBundle: MoveBundle,
        context: Context
    ): MoveResult {
        // Exit if 'manage all files' permission not granted
        if (!isExternalStorageManger) {
            return MoveResult.Failure.ManageAllFilesPermissionMissing
        }

        if (!moveBundle.file.mediaStoreData.fileExists) {
            return MoveResult.Failure.MoveFileNotFound
        }

        // Exit on unsuccessful conversion to SimpleStorage objects
        val moveDestinationDocumentFile = moveBundle.destination.documentFile(context)
        val moveMediaFile = moveBundle.file.simpleStorageMediaFile(context)

        if (moveDestinationDocumentFile == null || moveMediaFile == null) {
            return MoveResult.Failure.InternalError
        }

        // Exit if file already at selected location.
        if (moveDestinationDocumentFile.hasChild(  // TODO: optimizable?
                context = context,
                path = moveBundle.file.mediaStoreData.name,
                requiresWriteAccess = false
            )
        ) {
            return MoveResult.Failure.FileAlreadyAtMoveDestination
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

                        if (moveState.errorCode == SingleFileErrorCode.TARGET_FOLDER_NOT_FOUND && moveBundle.mode.isAuto) {
                            MoveResult.Failure.AutoMoveDestinationNotFound(moveBundle)
                        } else {
                            context.showToast(moveState.errorCode.name)
                            MoveResult.Failure.InternalError
                        }
                    }

                    is SingleFileResult.Completed -> {
                        MoveResult.Success(moveBundle)
                    }

                    else -> {
                        null
                    }
                }
            }
            .filterNotNull()
            .firstBlocking()
    }
}

private val onFileConflict = object : SingleFileConflictCallback<DocumentFile>() {}