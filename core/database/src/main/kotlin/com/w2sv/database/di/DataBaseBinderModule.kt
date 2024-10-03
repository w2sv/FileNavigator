package com.w2sv.database.di

import com.w2sv.database.repository.RoomMovedFileRepository
import com.w2sv.domain.repository.MovedFileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DataBaseBinderModule {

    @Binds
    fun bindsMoveEntryRepository(impl: RoomMovedFileRepository): MovedFileRepository
}