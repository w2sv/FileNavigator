package com.anggrayudi.storage.result

import androidx.annotation.FloatRange
import com.anggrayudi.storage.FileWrapper

/**
 * Created on 7/6/24
 * @author Anggrayudi Hardiannico A.
 */
sealed interface SingleFileResult {

    /**
     * Emitted when check whether file copy requirements are met is started.
     */
    data object Validating : SingleFileResult

    /**
     * Emitted after check whether file copy requirements are met successfully completed.
     */
    data object Preparing : SingleFileResult

    data object DeletingConflictedFile : SingleFileResult

    data class InProgress(
        @FloatRange(0.0, 100.0) val progress: Float,
        val bytesMoved: Long,
        val writeSpeed: Int
    ) :
        SingleFileResult

    sealed interface SourceFileDeletionResult : SingleFileResult {
        data object Success : SourceFileDeletionResult
        data object Failure : SourceFileDeletionResult

        companion object {
            internal fun get(success: Boolean): SourceFileDeletionResult =
                when (success) {
                    true -> Success
                    false -> Failure
                }
        }
    }

    @JvmInline
    value class Completed(val file: FileWrapper) : SingleFileResult

    data class Error(val errorCode: SingleFileError, val message: String? = null) :
        SingleFileResult
}

sealed interface SingleFileError {
    open class StoragePermissionMissing : SingleFileError
    data object SourceNotReadable : StoragePermissionMissing()
    data object TargetNotWritable : StoragePermissionMissing()
    data object SourceNotFound : SingleFileError
    data object TargetNotFound : SingleFileError
    data object UnknownIOError : SingleFileError
    data object Cancelled : SingleFileError
    data object SourceAlreadyAtTarget : SingleFileError
    data class NotEnoughSpaceOnTarget(val freeSpace: Long, val requiredSpace: Long) :
        SingleFileError
}
