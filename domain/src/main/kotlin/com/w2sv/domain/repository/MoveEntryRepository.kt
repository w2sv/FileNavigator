package com.w2sv.domain.repository

import com.w2sv.domain.model.MoveEntry
import kotlinx.coroutines.flow.Flow

interface MoveEntryRepository {
    suspend fun insert(entry: MoveEntry)
    fun getAllInDescendingOrder(): Flow<List<MoveEntry>>
    suspend fun delete(entry: MoveEntry)
    suspend fun deleteAll()
}