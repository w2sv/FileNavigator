package com.w2sv.datastore.di

import com.w2sv.datastore.repository.NavigatorRepositoryImpl
import com.w2sv.datastore.repository.PreferencesRepositoryImpl
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface DataStoreRepositoryBinderModule {

    @Binds
    fun bindsNavigatorRepository(impl: NavigatorRepositoryImpl): NavigatorRepository

    @Binds
    fun bindsPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}