package com.w2sv.domain.usecase

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface MoveDestinationPathConverterBinderModule {

    @Binds
    fun bindsMoveDestinationPathConverter(impl: MoveDestinationPathConverterImpl): MoveDestinationPathConverter
}
