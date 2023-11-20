package com.w2sv.data.storage.database.di

import android.content.Context
import androidx.room.Room
import com.w2sv.data.storage.database.AppDatabase
import com.w2sv.data.storage.database.dao.MoveEntryDao
import com.w2sv.data.storage.database.repository.DatabaseMoveEntryRepository
import com.w2sv.domain.interfaces.MoveEntryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Singleton
    @Provides
    fun appDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app-database"
        )
            .build()

    @Provides
    fun moveEntryDao(appDatabase: AppDatabase): MoveEntryDao =
        appDatabase.getMoveEntryDao()
}