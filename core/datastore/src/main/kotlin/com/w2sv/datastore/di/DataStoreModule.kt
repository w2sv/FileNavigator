package com.w2sv.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.migration.NavigatorPreferencesToProtoMigration
import com.w2sv.datastore.proto.navigatorconfig.NavigatorConfigProtoSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {

    @Singleton
    @Provides
    fun preferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(context.packageName) }
        )

    @Provides
    @Singleton
    internal fun navigatorConfigProtoDataStore(
        @ApplicationContext context: Context,
        preferencesDataStore: DataStore<Preferences>
    ): DataStore<NavigatorConfigProto> =
        DataStoreFactory.create(
            serializer = NavigatorConfigProtoSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler { NavigatorConfigProtoSerializer.defaultValue },
            produceFile = {
                context.dataStoreFile("navigator_config.pb")
            },
            migrations = listOf(
                NavigatorPreferencesToProtoMigration(
                    preferencesDataStore = preferencesDataStore
                )
            )
        )
}