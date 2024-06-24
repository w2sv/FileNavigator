package com.w2sv.datastore

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.common.utils.DocumentUri
import com.w2sv.datastore.proto.navigatorconfig.NavigatorConfigMapper
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.kotlinutils.coroutines.firstBlocking
import com.w2sv.kotlinutils.time.durationToNow
import com.w2sv.kotlinutils.time.localDateTimeFromMilliSecondsUnixTimestamp
import com.w2sv.kotlinutils.update
import slimber.log.i

internal class NavigatorPreferencesToProtoMigration(
    private val context: Context,
    private val preferencesDataStore: DataStore<Preferences>
) : DataMigration<NavigatorConfigProto> {

    override suspend fun shouldMigrate(currentData: NavigatorConfigProto): Boolean {
        return !currentData.hasBeenMigrated.also { i { "hasBeenMigrated=$it" } }
    }

    override suspend fun cleanUp() {}

    override suspend fun migrate(currentData: NavigatorConfigProto): NavigatorConfigProto {
        i { "Migrating" }

        return if (doFullMigration) {
            fullMigration(preferencesDataStore.data.firstBlocking())
        } else {
            currentData
        }
            .toBuilder().setHasBeenMigrated(true).build()
    }

    private val doFullMigration by lazy {
        localDateTimeFromMilliSecondsUnixTimestamp(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
                .firstInstallTime
        )
            .durationToNow()
            .toMinutes()
            .also { i { "Minutes delta: $it" } } > 5
    }

    private fun fullMigration(preferences: Preferences): NavigatorConfigProto {
        i { "Preferences content: ${preferences.asMap()}" }

        return NavigatorConfigMapper.toProto(
            NavigatorConfig.default.copy(
                disableOnLowBattery = preferences.getOrDefault(
                    booleanPreferencesKey(PreMigrationNavigatorPreferencesKey.DISABLE_ON_LOW_BATTERY),
                    true
                ),
                fileTypeConfigMap = NavigatorConfig.default.fileTypeConfigMap.toMutableMap().apply {
                    FileType.values.forEach { fileType ->
                        i { "Migrating $fileType" }
                        update(fileType) { fileTypeConfig ->
                            fileTypeConfig.copy(
                                enabled = preferences.getOrDefault(
                                    PreMigrationNavigatorPreferencesKey.fileTypeEnabled(fileType),
                                    true
                                ),
                                sourceTypeConfigMap = fileTypeConfig.sourceTypeConfigMap.toMutableMap()
                                    .apply {
                                        fileType.sourceTypes.forEach { sourceType ->
                                            i { "Migrating $fileType.$sourceType" }

                                            update(sourceType) { sourceConfig ->
                                                sourceConfig.copy(
                                                    enabled = preferences.getOrDefault(
                                                        PreMigrationNavigatorPreferencesKey.sourceTypeEnabled(
                                                            fileType,
                                                            sourceType
                                                        ),
                                                        true
                                                    ),
                                                    lastMoveDestinations = buildList {
                                                        preferences[PreMigrationNavigatorPreferencesKey.lastMoveDestination(
                                                            fileType,
                                                            sourceType
                                                        )]?.let { lastMoveDestination ->
                                                            add(
                                                                DocumentUri.parse(
                                                                    lastMoveDestination
                                                                )
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
            )
                .also { i { "Migrated: $it" } }
        )
    }
}

private object PreMigrationNavigatorPreferencesKey {

    const val DISABLE_ON_LOW_BATTERY = "disableNavigatorOnLowBattery"

    fun fileTypeEnabled(fileType: FileType): Preferences.Key<Boolean> =
        booleanPreferencesKey(fileType.preferencesKeyNameIdentifier)

    fun sourceTypeEnabled(
        fileType: FileType,
        sourceType: SourceType
    ): Preferences.Key<Boolean> =
        booleanPreferencesKey("${fileType.preferencesKeyNameIdentifier}.${sourceType.name}.IS_ENABLED")

    fun lastMoveDestination(
        fileType: FileType,
        sourceType: SourceType
    ): Preferences.Key<String> =
        stringPreferencesKey("${fileType.preferencesKeyNameIdentifier}.${sourceType.name}.LAST_MOVE_DESTINATION")

    private val FileType.preferencesKeyNameIdentifier: String
        get() = this::class.java.simpleName
}

private fun <K> Preferences.getOrDefault(k: Preferences.Key<K>, default: K): K =
    get(k) ?: default