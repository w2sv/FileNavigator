package com.w2sv.database.di

import android.content.Context
import androidx.room.Room
import com.w2sv.database.Migrations
import com.w2sv.database.dao.MoveEntryDao
import com.w2sv.database.AppDatabase
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
        Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "app-database"
            )
            .addMigrations(
                Migrations.Migration2to3(context = context)
            )
//            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun moveEntryDao(appDatabase: AppDatabase): MoveEntryDao =
        appDatabase.getMoveEntryDao()
}