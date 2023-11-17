package com.w2sv.domain

import com.w2sv.data.model.MoveEntry
import com.w2sv.data.storage.database.dao.MoveEntryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InsertMoveEntryUseCase @Inject constructor(
    private val moveEntryDao: MoveEntryDao
) {
    suspend operator fun invoke(moveEntry: MoveEntry) {
        withContext(Dispatchers.IO) {
            moveEntryDao.insert(moveEntry)
        }
    }
}