package com.w2sv.datastorage.database.repository

import com.w2sv.datastorage.database.dao.MoveEntryDao
import com.w2sv.datastorage.database.model.MoveEntryEntity
import com.w2sv.domain.model.MoveEntry
import com.w2sv.domain.repository.MoveEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DatabaseMoveEntryRepository @Inject constructor(
    private val moveEntryDao: MoveEntryDao
) : MoveEntryRepository {

    override suspend fun insert(entry: MoveEntry) {
        moveEntryDao.insert(MoveEntryEntity(entry))
    }

    override suspend fun delete(entry: MoveEntry) {
        moveEntryDao.delete(MoveEntryEntity(entry))
    }

    override suspend fun deleteAll() {
        moveEntryDao.deleteAll()
    }

    override fun getAllInDescendingOrder(): Flow<List<MoveEntry>> =
        moveEntryDao
            .loadAllInDescendingOrder()
            .map { it.map { entity -> entity.asExternalModel() } }
}