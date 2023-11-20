package com.w2sv.data.storage.database.di

import com.w2sv.data.storage.database.repository.DatabaseMoveEntryRepository
import com.w2sv.domain.interfaces.MoveEntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindsMoveEntryRepository(databaseMoveEntryRepository: DatabaseMoveEntryRepository): MoveEntryRepository
}