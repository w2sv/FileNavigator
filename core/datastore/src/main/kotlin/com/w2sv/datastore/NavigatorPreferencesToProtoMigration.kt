package com.w2sv.datastore

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.datastore.proto.navigatorconfig.NavigatorConfigMapper
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.kotlinutils.update
import kotlinx.coroutines.flow.first
import slimber.log.i

internal class NavigatorPreferencesToProtoMigration(
    private val preferencesDataStore: DataStore<Preferences>
) : DataMigration<NavigatorConfigProto> {

    override suspend fun shouldMigrate(currentData: NavigatorConfigProto): Boolean =
        !currentData.hasBeenMigrated.also { i { "hasBeenMigrated=$it" } }

    private lateinit var presentPreMigrationKeys: List<Preferences.Key<*>>

    override suspend fun migrate(currentData: NavigatorConfigProto): NavigatorConfigProto {
        i { "Migrating" }

        val preferences = preferencesDataStore.data.first()

        presentPreMigrationKeys = PreMigrationNavigatorPreferencesKey
            .keys()
            .filter { key -> preferences.contains(key) }
            .toList()
            .also { i { "presentPreMigrationKeys: $it" } }

        return if (presentPreMigrationKeys.isNotEmpty()) {
            performMigration(preferences)
        } else {
            currentData
        }
            .toBuilder()
            .setHasBeenMigrated(true)
            .build()
    }

    private fun performMigration(preferences: Preferences): NavigatorConfigProto {
        i { "Preferences content: ${preferences.asMap()}" }

        return NavigatorConfigMapper.toProto(
            NavigatorConfig.default.copy(
                disableOnLowBattery = preferences.getOrDefault(
                    PreMigrationNavigatorPreferencesKey.disableOnLowBattery,
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
                                                                MoveDestination.parse(
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

    override suspend fun cleanUp() {
        preferencesDataStore.updateData {
            it
                .toMutablePreferences()
                .apply {
                    presentPreMigrationKeys.forEach { key -> remove(key) }
                }
        }

        i { "Preferences post cleanUp: ${preferencesDataStore.data.first().asMap()}" }
    }
}

private object PreMigrationNavigatorPreferencesKey {

    val disableOnLowBattery = booleanPreferencesKey("disableNavigatorOnLowBattery")

    fun keys(): Sequence<Preferences.Key<*>> =
        sequence {
            yield(disableOnLowBattery)
            yieldAll(
                FileType
                    .values
                    .flatMap { it.associatedPreMigrationKeys() }
            )
        }

    private fun FileType.associatedPreMigrationKeys(): Sequence<Preferences.Key<*>> = sequence {
        yield(fileTypeEnabled(this@associatedPreMigrationKeys))

        this@associatedPreMigrationKeys.sourceTypes.forEach { sourceType ->
            yield(
                sourceTypeEnabled(
                    this@associatedPreMigrationKeys,
                    sourceType
                )
            )
            yield(
                lastMoveDestination(
                    this@associatedPreMigrationKeys,
                    sourceType
                )
            )
        }
    }

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