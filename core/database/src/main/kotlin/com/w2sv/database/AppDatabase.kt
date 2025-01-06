package com.w2sv.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.w2sv.database.dao.MovedFileDao
import com.w2sv.database.entity.MovedFileEntity
import com.w2sv.database.typeconverter.FileTypeConverter
import com.w2sv.database.typeconverter.LocalDateTimeConverter
import com.w2sv.database.typeconverter.UriConverter

@Database(
    entities = [MovedFileEntity::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(
    LocalDateTimeConverter::class,
    UriConverter::class,
    FileTypeConverter::class
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun getMovedFileDao(): MovedFileDao
}
