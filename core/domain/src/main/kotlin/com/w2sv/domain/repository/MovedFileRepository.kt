package com.w2sv.domain.repository

import com.w2sv.domain.model.MovedFile
import kotlinx.coroutines.flow.Flow

interface MovedFileRepository {
    suspend fun insert(file: MovedFile)
    fun getAllInDescendingOrder(): Flow<List<MovedFile>>
    suspend fun delete(file: MovedFile)
    suspend fun deleteAll()
}
