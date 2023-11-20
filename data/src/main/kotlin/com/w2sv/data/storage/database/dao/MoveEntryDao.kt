package com.w2sv.data.storage.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.w2sv.data.storage.database.model.MoveEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoveEntryDao {
    @Query("SELECT * FROM MoveEntryEntity ORDER BY dateTime DESC")
    fun loadAllInDescendingOrder(): Flow<List<MoveEntryEntity>>

    @Insert
    fun insert(entry: MoveEntryEntity)

    @Query("DELETE FROM MoveEntryEntity")
    fun deleteAll()

    @Delete
    fun delete(entry: MoveEntryEntity)
}
