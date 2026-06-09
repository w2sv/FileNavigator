package com.w2sv.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.core.di.ApplicationDefaultScope
import com.w2sv.domain.model.settings.AppSettings
import com.w2sv.domain.model.settings.Theme
import com.w2sv.domain.model.settings.ThemeSettings
import com.w2sv.domain.repository.AppSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
internal class AppSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationDefaultScope scope: CoroutineScope
) : AppSettingsRepository {

    private val showStorageVolumeNamesKey = booleanPreferencesKey("showStorageVolumeNames")
    private val themeKey = intPreferencesKey("theme")
    private val useAmoledBlackThemeKey = booleanPreferencesKey("useAmoledBlackTheme")
    private val useDynamicColorsKey = booleanPreferencesKey("useDynamicColors")

    override val appSettings: StateFlow<AppSettings> = dataStore.data
        .map { it.toAppSettings() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings.Default
        )

    override suspend fun updateAppSettings(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { preferences ->
            val settings = transform(preferences.toAppSettings())
            preferences[showStorageVolumeNamesKey] = settings.showStorageVolumeNames
            preferences[themeKey] = settings.theme.theme.ordinal
            preferences[useAmoledBlackThemeKey] = settings.theme.useAmoledBlackTheme
            preferences[useDynamicColorsKey] = settings.theme.useDynamicColors
        }
    }

    private fun Preferences.toAppSettings(): AppSettings =
        AppSettings(
            showStorageVolumeNames = this[showStorageVolumeNamesKey] ?: AppSettings.Default.showStorageVolumeNames,
            theme = toThemeSettings()
        )

    private fun Preferences.toThemeSettings(): ThemeSettings {
        val defaults = AppSettings.Default.theme
        return ThemeSettings(
            theme = this[themeKey]?.let(Theme.entries::getOrNull) ?: defaults.theme,
            useAmoledBlackTheme = this[useAmoledBlackThemeKey] ?: defaults.useAmoledBlackTheme,
            useDynamicColors = (this[useDynamicColorsKey] ?: defaults.useDynamicColors) && defaults.useDynamicColors
        )
    }
}
