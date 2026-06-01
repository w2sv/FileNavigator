package com.w2sv.usecase.di

import com.w2sv.domain.usecase.GetMoveHistoryUseCase
import com.w2sv.domain.usecase.InsertMovedFileUseCase
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import com.w2sv.usecase.GetMoveHistoryUseCaseImpl
import com.w2sv.usecase.InsertMovedFileUseCaseImpl
import com.w2sv.usecase.MoveDestinationLabelProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface UseCaseBinderModule {

    @Binds
    fun bindsGetMoveHistoryUseCase(impl: GetMoveHistoryUseCaseImpl): GetMoveHistoryUseCase

    @Binds
    fun bindsInsertMovedFileUseCase(impl: InsertMovedFileUseCaseImpl): InsertMovedFileUseCase

    @Binds
    fun bindsMoveDestinationLabelProvider(impl: MoveDestinationLabelProviderImpl): MoveDestinationLabelProvider
}
