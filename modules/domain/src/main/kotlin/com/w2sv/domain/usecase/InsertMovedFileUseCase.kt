package com.w2sv.domain.usecase

import com.w2sv.domain.model.MovedFile

/**
 * Persists a completed move operation so it can appear in the user's move history.
 */
interface InsertMovedFileUseCase {
    suspend operator fun invoke(movedFile: MovedFile)
}
