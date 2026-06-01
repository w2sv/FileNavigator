package com.w2sv.usecase

import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.repository.MovedFileRepository
import com.w2sv.domain.usecase.InsertMovedFileUseCase
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class InsertMovedFileUseCaseImpl @Inject constructor(private val movedFileRepository: MovedFileRepository) :
    InsertMovedFileUseCase {

    override suspend operator fun invoke(movedFile: MovedFile) {
        withContext(Dispatchers.IO) {
            movedFileRepository.insert(movedFile)
        }
    }
}
