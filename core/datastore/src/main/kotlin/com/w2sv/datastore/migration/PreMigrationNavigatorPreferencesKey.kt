package com.w2sv.datastore.migration

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType

internal object PreMigrationNavigatorPreferencesKey {

    val disableOnLowBattery = booleanPreferencesKey("disableNavigatorOnLowBattery")

    /**
     * @return All historically used [Preferences.Key]s related to the navigator configuration.
     */
    fun keys(): Sequence<Preferences.Key<*>> =
        sequence {
            yield(disableOnLowBattery)
            yieldAll(PresetFileType.values.flatMap { it.associatedPreMigrationKeys() })
        }

    private fun PresetFileType.associatedPreMigrationKeys(): Sequence<Preferences.Key<*>> =
        sequence {
            yield(fileTypeEnabled(this@associatedPreMigrationKeys))

            sourceTypes.forEach { sourceType ->
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

    fun fileTypeEnabled(fileType: PresetFileType): Preferences.Key<Boolean> =
        booleanPreferencesKey(fileType.preferencesKeyNameIdentifier)

    fun sourceTypeEnabled(fileType: PresetFileType, sourceType: SourceType): Preferences.Key<Boolean> =
        booleanPreferencesKey("${fileType.preferencesKeyNameIdentifier}.${sourceType.name}.IS_ENABLED")

    fun lastMoveDestination(fileType: PresetFileType, sourceType: SourceType): Preferences.Key<String> =
        stringPreferencesKey("${fileType.preferencesKeyNameIdentifier}.${sourceType.name}.LAST_MOVE_DESTINATION")

    private val PresetFileType.preferencesKeyNameIdentifier: String
        get() = this::class.java.simpleName
}
