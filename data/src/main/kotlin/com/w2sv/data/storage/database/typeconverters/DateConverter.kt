package com.w2sv.data.storage.database.typeconverters

import androidx.room.TypeConverter
import java.util.Date

object DateConverter {
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(timestamp: Long): Date {
        return Date(timestamp)
    }
}