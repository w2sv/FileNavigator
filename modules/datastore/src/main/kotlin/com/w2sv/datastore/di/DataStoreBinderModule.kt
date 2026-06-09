package com.w2sv.datastore.di

import com.w2sv.datastore.preferences.AppSettingsRepositoryImpl
import com.w2sv.datastore.preferences.RequestFlagRepositoryImpl
import com.w2sv.datastore.proto.navigatorconfig.NavigatorConfigDataSourceImpl
import com.w2sv.domain.repository.AppSettingsRepository
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.repository.RequestFlagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface DataStoreBinderModule {

    @Binds
    fun bindsNavigatorConfigDataSource(impl: NavigatorConfigDataSourceImpl): NavigatorConfigDataSource

    @Binds
    fun bindsAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository

    @Binds
    fun bindsRequestFlagRepository(impl: RequestFlagRepositoryImpl): RequestFlagRepository
}
