package com.w2sv.domain.usecase

import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.repository.MovedFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InsertMovedFileUseCase @Inject constructor(
    private val movedFileRepository: MovedFileRepository
) {
    suspend operator fun invoke(movedFile: MovedFile) {
        withContext(Dispatchers.IO) {
            movedFileRepository.insert(movedFile)
        }
    }
}