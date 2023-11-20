package com.w2sv.domain.usecase

import com.w2sv.domain.interfaces.MoveEntryRepository
import com.w2sv.domain.model.MoveEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InsertMoveEntryUseCase @Inject constructor(
    private val moveEntryRepository: MoveEntryRepository
) {
    suspend operator fun invoke(moveEntry: MoveEntry) {
        withContext(Dispatchers.IO) {
            moveEntryRepository.insert(moveEntry)
        }
    }
}