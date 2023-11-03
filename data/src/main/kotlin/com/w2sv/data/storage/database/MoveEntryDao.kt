package com.w2sv.data.storage.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.w2sv.data.model.MoveEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MoveEntryDao {
    @Query("SELECT * FROM MoveEntry ORDER BY date DESC")
    fun loadAllInDescendingOrder(): Flow<List<MoveEntry>>

    @Insert
    fun insert(entry: MoveEntry)

    @Query("DELETE FROM MoveEntry")
    fun deleteAll()
}
