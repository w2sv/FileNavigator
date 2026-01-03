package com.w2sv.domain.usecase.di

import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.domain.usecase.MoveDestinationPathConverterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface UseCaseBinderModule {

    @Binds
    fun bindsMoveDestinationPathConverter(impl: MoveDestinationPathConverterImpl): MoveDestinationPathConverter
}
