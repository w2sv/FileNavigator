package com.w2sv.domain.usecase

import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.repository.MovedFileRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InsertMovedFileUseCase @Inject constructor(
    private val movedFileRepository: MovedFileRepository
) {
    suspend operator fun invoke(movedFile: MovedFile) {
        withContext(Dispatchers.IO) {
            movedFileRepository.insert(movedFile)
        }
    }
}
