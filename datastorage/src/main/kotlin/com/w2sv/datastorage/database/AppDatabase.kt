package com.w2sv.datastorage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.w2sv.datastorage.database.dao.MoveEntryDao
import com.w2sv.datastorage.database.model.MoveEntryEntity
import com.w2sv.datastorage.database.typeconverter.FileTypeConverter
import com.w2sv.datastorage.database.typeconverter.LocalDateTimeConverter
import com.w2sv.datastorage.database.typeconverter.UriConverter

@Database(
    entities = [MoveEntryEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(
    LocalDateTimeConverter::class,
    UriConverter::class,
    FileTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMoveEntryDao(): MoveEntryDao
}