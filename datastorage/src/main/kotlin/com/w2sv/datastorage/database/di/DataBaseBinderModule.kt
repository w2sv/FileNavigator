package com.w2sv.datastorage.database.di

import com.w2sv.datastorage.database.repository.DatabaseMoveEntryRepository
import com.w2sv.domain.repository.MoveEntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataBaseBinderModule {

    @Binds
    fun bindsMoveEntryRepository(databaseMoveEntryRepository: DatabaseMoveEntryRepository): MoveEntryRepository
}