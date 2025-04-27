package com.w2sv.datastore.migration

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.common.util.log
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.proto.navigatorconfig.toProto
import com.w2sv.domain.model.filetype.AnyPresetWrappingFileType
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import kotlinx.coroutines.flow.first
import slimber.log.i

internal class NavigatorPreferencesToProtoMigration(private val preferencesDataStore: DataStore<Preferences>) :
    DataMigration<NavigatorConfigProto> {

    override suspend fun shouldMigrate(currentData: NavigatorConfigProto): Boolean =
        !currentData.hasBeenMigrated.log { "hasBeenMigrated=$it" }

    private lateinit var presentPreMigrationKeys: List<Preferences.Key<*>>

    override suspend fun migrate(currentData: NavigatorConfigProto): NavigatorConfigProto {
        i { "Migrating" }

        val preferences = preferencesDataStore.data.first()

        presentPreMigrationKeys = PreMigrationNavigatorPreferencesKey
            .keys()
            .filter { key -> preferences.contains(key) }
            .toList()
            .log { "presentPreMigrationKeys: $it" }

        return if (presentPreMigrationKeys.isNotEmpty()) {
            performMigration(preferences)
        } else {
            currentData
                .toBuilder()
                .setHasBeenMigrated(true)
                .build()
        }
    }

    private fun performMigration(preferences: Preferences): NavigatorConfigProto =
        NavigatorConfig.default.let { defaultConfig ->
            defaultConfig.copy(
                fileTypeConfigMap = defaultConfig.fileTypeConfigMap.mapValues { (fileType, fileTypeConfig) ->
                    // default config contains only AnyPresetWrappingFileTypes
                    fileType as AnyPresetWrappingFileType
                    val presetFileType = fileType.presetFileType

                    fileTypeConfig.copy(
                        enabled = preferences.getOrDefault(
                            PreMigrationNavigatorPreferencesKey.fileTypeEnabled(presetFileType),
                            fileTypeConfig.enabled
                        ),
                        sourceTypeConfigMap = fileTypeConfig.sourceTypeConfigMap.mapValues { (sourceType, sourceConfig) ->
                            sourceConfig.copy(
                                enabled = preferences.getOrDefault(
                                    PreMigrationNavigatorPreferencesKey.sourceTypeEnabled(
                                        fileType = presetFileType,
                                        sourceType = sourceType
                                    ),
                                    sourceConfig.enabled
                                ),
                                quickMoveDestinations = preferences[
                                    PreMigrationNavigatorPreferencesKey.lastMoveDestination(
                                        fileType = presetFileType,
                                        sourceType = sourceType
                                    )
                                ]
                                    ?.let { lastMoveDestination -> listOf(LocalDestination.parse(lastMoveDestination)) }
                                    ?: emptyList()
                            )
                        }
                    )
                },
                disableOnLowBattery = preferences.getOrDefault(
                    PreMigrationNavigatorPreferencesKey.disableOnLowBattery,
                    defaultConfig.disableOnLowBattery
                )
            )
        }
            .log { "Migrated: $it" }
            .toProto(hasBeenMigrated = true)

    override suspend fun cleanUp() {
        preferencesDataStore.updateData {
            it
                .toMutablePreferences()
                .apply { presentPreMigrationKeys.forEach { key -> remove(key) } }
        }

        i { "Preferences post cleanUp: ${preferencesDataStore.data.first().asMap()}" }
    }
}

private fun <K> Preferences.getOrDefault(k: Preferences.Key<K>, default: K): K =
    get(k) ?: default
