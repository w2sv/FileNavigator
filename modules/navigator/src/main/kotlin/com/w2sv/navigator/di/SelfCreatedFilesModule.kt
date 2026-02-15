package com.w2sv.navigator.di

import com.w2sv.navigator.shared.createdfiles.EmitSelfCreatedFile
import com.w2sv.navigator.shared.createdfiles.SelfCreatedFileIdentifiers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class SelfCreatedFilesFlow

@Module
@InstallIn(SingletonComponent::class)
internal object SelfCreatedFilesModule {

    @Provides
    @Singleton
    fun provideMutableSelfCreatedFilesFlow(): MutableSharedFlow<SelfCreatedFileIdentifiers> =
        MutableSharedFlow(extraBufferCapacity = 16)

    @SelfCreatedFilesFlow
    @Provides
    fun provideSelfCreatedFilesFlow(flow: MutableSharedFlow<SelfCreatedFileIdentifiers>): Flow<SelfCreatedFileIdentifiers> =
        flow.asSharedFlow()

    @Provides
    fun provideEmitSelfCreatedFile(flow: MutableSharedFlow<SelfCreatedFileIdentifiers>): EmitSelfCreatedFile =
        EmitSelfCreatedFile { value -> flow.emit(value) }
}
