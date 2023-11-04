package com.w2sv.data.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.w2sv.data.model.MoveEntry
import com.w2sv.data.storage.database.typeconverters.FileTypeConverter
import com.w2sv.data.storage.database.typeconverters.LocalDateTimeConverter
import com.w2sv.data.storage.database.typeconverters.UriConverter

@Database(
    entities = [MoveEntry::class],
    version = 1
)
@TypeConverters(
    LocalDateTimeConverter::class,
    UriConverter::class,
    FileTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMoveEntryDao(): MoveEntryDao
}