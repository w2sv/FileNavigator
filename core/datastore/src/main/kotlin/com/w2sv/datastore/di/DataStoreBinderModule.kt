package com.w2sv.datastore.di

import com.w2sv.datastore.navigatorconfig.NavigatorConfigDataSourceImpl
import com.w2sv.datastore.preferences.PreferencesRepositoryImpl
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface DataStoreBinderModule {

    @Binds
    fun bindsNavigatorConfigDataSource(impl: NavigatorConfigDataSourceImpl): NavigatorConfigDataSource

    @Binds
    fun bindsPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}