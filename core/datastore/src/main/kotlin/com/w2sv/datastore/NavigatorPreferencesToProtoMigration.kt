package com.w2sv.datastore

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.coroutines.firstBlocking
import com.w2sv.androidutils.generic.timeDeltaToNow
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.update
import com.w2sv.datastore.proto.navigatorconfig.NavigatorConfigMapper
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import slimber.log.i
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

internal class NavigatorPreferencesToProtoMigration(
    private val context: Context,
    private val preferencesDataStore: DataStore<Preferences>
) : DataMigration<NavigatorConfigProto> {

    override suspend fun shouldMigrate(currentData: NavigatorConfigProto): Boolean {
        return (!currentData.hasBeenMigrated.also { i { "hasBeenMigrated=$it" } } && localDateTimeFromUnixMilliSecondsTimeStamp(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
                .firstInstallTime
        )
            .timeDeltaToNow().toMinutes().also { i { "Minutes delta: $it" } } > 5)
            .also { i { "Should migrate=$it" } }
    }

    override suspend fun cleanUp() {}

    override suspend fun migrate(currentData: NavigatorConfigProto): NavigatorConfigProto {
        i { "Migrating" }

        val preferences = preferencesDataStore.data.firstBlocking()

        i { "Preferences content: ${preferences.asMap()}" }

        return NavigatorConfigMapper.toProto(
            NavigatorConfig.default.copy(
                disableOnLowBattery = preferences[booleanPreferencesKey("disableNavigatorOnLowBattery")]
                    ?: NavigatorConfig.default.disableOnLowBattery,
                fileTypeConfigMap = NavigatorConfig.default.fileTypeConfigMap.toMutableMap().apply {
                    FileType.values.forEach { fileType ->
                        i { "Migrating $fileType" }
                        update(fileType) { fileTypeConfig ->
                            val fileTypeEnabled =
                                preferences.getOrDefault(fileType.preferencesKey, true)
                            fileTypeConfig.copy(
                                enabled = fileTypeEnabled,
                                sourceTypeConfigMap = fileTypeConfig.sourceTypeConfigMap.toMutableMap()
                                    .apply {
                                        fileType.sourceTypes.forEach { sourceType ->
                                            preferences[sourceTypeEnabledPreferencesKey(
                                                fileType,
                                                sourceType
                                            )]?.let { sourceTypeEnabled ->
                                                i { "Migrating $fileType.$sourceType" }
                                                update(sourceType) { sourceConfig ->
                                                    sourceConfig.copy(
                                                        enabled = sourceTypeEnabled,
                                                        lastMoveDestinations = buildList {
                                                            preferences[lastMoveDestinationPreferencesKey(
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
                                    }
                            )
                        }
                    }
                }
            )
                .also { i { "Migrated: $it" } },
            hasBeenMigrated = true
        )
    }
}

private fun localDateTimeFromUnixMilliSecondsTimeStamp(
    msTimeStamp: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalDateTime =
    LocalDateTime.ofInstant(
        Instant.ofEpochMilli(msTimeStamp),
        zoneId
    )

private fun <K> Preferences.getOrDefault(k: Preferences.Key<K>, default: K): K =
    get(k) ?: default

private val FileType.preferencesIdentifier: String
    get() = this::class.java.simpleName

private val FileType.preferencesKey: Preferences.Key<Boolean>
    get() = booleanPreferencesKey(preferencesIdentifier)

private fun sourceTypeEnabledPreferencesKey(
    fileType: FileType,
    sourceType: SourceType
): Preferences.Key<Boolean> =
    booleanPreferencesKey("${fileType.preferencesIdentifier}.${sourceType.name}.IS_ENABLED")

private fun lastMoveDestinationPreferencesKey(
    fileType: FileType,
    sourceType: SourceType
): Preferences.Key<String> =
    stringPreferencesKey("${fileType.preferencesIdentifier}.${sourceType.name}.LAST_MOVE_DESTINATION")