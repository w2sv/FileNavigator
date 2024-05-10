package com.w2sv.datastorage.preferences.di

import com.w2sv.datastorage.preferences.repository.NavigatorRepositoryImpl
import com.w2sv.datastorage.preferences.repository.PreferencesRepositoryImpl
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface PreferencesBinderModule {

    @Binds
    fun bindsNavigatorRepository(impl: NavigatorRepositoryImpl): NavigatorRepository

    @Binds
    fun bindsPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}