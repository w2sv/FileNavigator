package com.w2sv.database.repository

import com.w2sv.database.dao.MovedFileDao
import com.w2sv.database.entity.MovedFileEntity
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.repository.MovedFileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class RoomMovedFileRepository @Inject constructor(
    private val movedFileDao: MovedFileDao
) : MovedFileRepository {

    override suspend fun insert(file: MovedFile) {
        movedFileDao.insert(MovedFileEntity(file))
    }

    override suspend fun delete(file: MovedFile) {
        movedFileDao.delete(MovedFileEntity(file))
    }

    override suspend fun deleteAll() {
        movedFileDao.deleteAll()
    }

    override fun getAllInDescendingOrder(): Flow<List<MovedFile>> =
        movedFileDao
            .loadAllInDescendingOrder()
            .map { it.map { entity -> entity.asExternal() } }
}