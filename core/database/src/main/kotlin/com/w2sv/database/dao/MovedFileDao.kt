package com.w2sv.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.w2sv.database.entity.MovedFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MovedFileDao {
    @Query("SELECT * FROM MovedFileEntity ORDER BY moveDateTime DESC")
    fun loadAllInDescendingOrder(): Flow<List<MovedFileEntity>>

    @Insert
    fun insert(entry: MovedFileEntity)

    @Query("DELETE FROM MovedFileEntity")
    fun deleteAll()

    @Delete
    fun delete(entry: MovedFileEntity)
}
