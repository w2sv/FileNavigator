package com.w2sv.navigator.moving

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.result.SingleFileErrorCode
import com.anggrayudi.storage.result.SingleFileResult
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.kotlinutils.coroutines.firstBlocking
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import slimber.log.i

internal sealed interface PreMoveCheckResult {
    @JvmInline
    value class Success(val documentFile: DocumentFile) : PreMoveCheckResult

    abstract class Failure(val failure: MoveResult.Failure) : PreMoveCheckResult

    data object ManageAllFilesPermissionMissing :
        Failure(MoveResult.Failure.ManageAllFilesPermissionMissing)

    data object InternalError : Failure(MoveResult.Failure.InternalError)
}

internal fun sharedPreMoveChecks(destination: DocumentUri, context: Context): PreMoveCheckResult {
    val documentFile = destination.documentFile(context)

    return when {
        !isExternalStorageManger -> PreMoveCheckResult.ManageAllFilesPermissionMissing
        documentFile == null -> PreMoveCheckResult.InternalError
        else -> PreMoveCheckResult.Success(documentFile)
    }
}

internal fun MoveBundle.move(context: Context): MoveResult {
    val destinationDocumentFile = sharedPreMoveChecks(destination, context).run {
        when (this) {
            is PreMoveCheckResult.Success -> documentFile
            is PreMoveCheckResult.Failure -> return failure
        }
    }

    return file.moveTo(
        destination = destinationDocumentFile,
        context = context,
        makeMoveBundle = { this }
    )
}

internal fun MoveFile.moveTo(
    destination: DocumentFile,
    context: Context,
    makeMoveBundle: () -> MoveBundle
): MoveResult {
    if (!mediaStoreData.fileExists) {
        return MoveResult.Failure.MoveFileNotFound
    }

    val mediaFile = simpleStorageMediaFile(context) ?: return MoveResult.Failure.InternalError

    // Exit if file already at destination
    if (destination.hasChild(  // TODO: optimizable?
            context = context,
            path = mediaStoreData.name,
            requiresWriteAccess = false
        )
    ) {
        return MoveResult.Failure.FileAlreadyAtDestination
    }

    return mediaFile.moveTo(
        targetFolder = destination,
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
                            makeMoveBundle()
                        )

                        SingleFileErrorCode.NO_SPACE_LEFT_ON_TARGET_PATH -> MoveResult.Failure.NotEnoughSpaceOnDestination
                        SingleFileErrorCode.SOURCE_FILE_NOT_FOUND -> MoveResult.Failure.MoveFileNotFound
                        SingleFileErrorCode.STORAGE_PERMISSION_DENIED, SingleFileErrorCode.CANNOT_CREATE_FILE_IN_TARGET -> MoveResult.Failure.ManageAllFilesPermissionMissing
                        else -> MoveResult.Failure.InternalError
                    }
                }

                is SingleFileResult.Completed -> {
                    MoveResult.Success(makeMoveBundle())
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