package com.w2sv.datastore.di

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.w2sv.androidutils.coroutines.firstBlocking
import com.w2sv.androidutils.generic.localDateTimeFromUnixTimeStamp
import com.w2sv.androidutils.generic.timeDeltaToNow
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.fileTypeConfigProto
import com.w2sv.datastore.navigatorConfigProto
import com.w2sv.datastore.proto.navigatorconfig.NavigatorConfigProtoSerializer
import com.w2sv.datastore.sourceConfigProto
import com.w2sv.domain.model.FileType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import slimber.log.i
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
                object : DataMigration<NavigatorConfigProto> {
                    override suspend fun shouldMigrate(currentData: NavigatorConfigProto): Boolean {
                        return (!currentData.hasBeenMigrated && localDateTimeFromUnixTimeStamp(
                            context.packageManager.getPackageInfo(
                                context.packageName,
                                0
                            )
                                .firstInstallTime / 1000L
                        )
                            .timeDeltaToNow().toMinutes() > 5)
                            .also { i { "Should migrate=$it" } }
                    }

                    override suspend fun cleanUp() {}

                    override suspend fun migrate(currentData: NavigatorConfigProto): NavigatorConfigProto {
                        i { "Migrating" }

                        val preferences = preferencesDataStore.data.firstBlocking()

                        return navigatorConfigProto {
                            disableOnLowBattery =
                                preferences[booleanPreferencesKey("disableNavigatorOnLowBattery")]
                                    ?: false

                            FileType.values.forEach { fileType ->
                                val fileTypePreferencesStoreIdentifier =
                                    fileType::class.java.simpleName
                                preferences[booleanPreferencesKey(fileTypePreferencesStoreIdentifier)]?.let { fileTypeEnabled ->
                                    fileTypeToConfig[FileType.values.indexOf(fileType)] =
                                        fileTypeConfigProto {
                                            enabled = fileTypeEnabled
                                            fileType.sourceTypes.forEach { sourceType ->
                                                preferences[booleanPreferencesKey("${fileTypePreferencesStoreIdentifier}.${sourceType.name}.IS_ENABLED")]?.let { sourceTypeEnabled ->
                                                    sourceTypeToConfig[sourceType.ordinal] =
                                                        sourceConfigProto {
                                                            enabled = sourceTypeEnabled
                                                        }
                                                }
                                            }
                                        }
                                }
                            }
                            hasBeenMigrated = true
                        }
                    }
                }
            )
        )
}