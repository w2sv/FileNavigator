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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.w2sv.androidutils.coroutines.firstBlocking
import com.w2sv.androidutils.generic.localDateTimeFromUnixTimeStamp
import com.w2sv.androidutils.generic.timeDeltaToNow
import com.w2sv.common.utils.update
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.copy
import com.w2sv.datastore.proto.navigatorconfig.NavigatorConfigMapper
import com.w2sv.datastore.proto.navigatorconfig.NavigatorConfigProtoSerializer
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
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
                        return (!currentData.hasBeenMigrated.also { i { "hasBeenMigrated=$it" } } && localDateTimeFromUnixTimeStamp(
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

                        i { "Preferences content: ${preferences.asMap()}" }

                        var migrated = NavigatorConfigMapper.toProto(NavigatorConfig.default)
                        preferences[booleanPreferencesKey("disableNavigatorOnLowBattery")]?.let {
                            migrated = migrated.copy { disableOnLowBattery = it }
                        }

                        // Migrate file type configs
                        FileType.values.forEach { fileType ->
                            i { "Migrating $fileType" }

                            val fileTypeEnabled =
                                preferences.getOrDefault(fileType.preferencesKey, true)

                            migrated.fileTypeToConfigMap.update(
                                FileType.values.indexOf(
                                    fileType
                                )
                            ) { fileTypeConfig ->
                                fileTypeConfig.copy {
                                    this.enabled = fileTypeEnabled
                                    // Migrate source configs
                                    fileType.sourceTypes.forEach { sourceType ->
                                        preferences[sourceEnabledPreferencesKey(
                                            fileType,
                                            sourceType
                                        )]?.let { sourceTypeEnabled ->
                                            i { "Migrating $fileType.$sourceType" }
                                            this.sourceTypeToConfig.toMutableMap()[sourceType.ordinal] =
                                                this.sourceTypeToConfig.getValue(sourceType.ordinal)
                                                    .copy {
                                                        this.enabled = sourceTypeEnabled
                                                        // Migrate lastMoveDestination
                                                        preferences[lastMoveDestinationPreferencesKey(
                                                            fileType,
                                                            sourceType
                                                        )]?.let { lastMoveDestination ->
                                                            this.lastMoveDestinations.add(
                                                                lastMoveDestination
                                                            )
                                                        }
                                                    }
                                        }
                                    }
                                }
                            }
                        }
                        i { "Migrated: ${NavigatorConfigMapper.toExternal(migrated)}" }

//                        migrated = migrated.copy { hasBeenMigrated = true }

                        return migrated
                    }
                }
            )
        )
}

private fun <K> Preferences.getOrDefault(k: Preferences.Key<K>, default: K): K =
    get(k) ?: default

private val FileType.preferencesIdentifier: String
    get() = this::class.java.simpleName

private val FileType.preferencesKey: Preferences.Key<Boolean>
    get() = booleanPreferencesKey(preferencesIdentifier)

private fun sourceEnabledPreferencesKey(
    fileType: FileType,
    sourceType: SourceType
): Preferences.Key<Boolean> =
    booleanPreferencesKey("${fileType.preferencesIdentifier}.${sourceType.name}.IS_ENABLED")

private fun lastMoveDestinationPreferencesKey(
    fileType: FileType,
    sourceType: SourceType
): Preferences.Key<String> =
    stringPreferencesKey("${fileType.preferencesIdentifier}.${sourceType.name}.LAST_MOVE_DESTINATION")