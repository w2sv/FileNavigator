package com.w2sv.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.common.di.ApplicationDefaultScope
import com.w2sv.domain.model.settings.AppSettings
import com.w2sv.domain.model.settings.Theme
import com.w2sv.domain.model.settings.ThemeSettings
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.coroutines.flow.combineStates
import com.w2sv.persistedpreferences.EnumSavePolicy
import com.w2sv.persistedpreferences.PersistedPreferencesAccessor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import slimber.log.i

@Singleton
internal class PreferencesRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>,
    @ApplicationDefaultScope scope: CoroutineScope
) : PersistedPreferencesAccessor(dataStore, log = ::i),
    PreferencesRepository {

    private val theme = enumPreference("theme", { Theme.Default }, EnumSavePolicy.byOrdinal())

    private val useAmoledBlackTheme = persistedPreference(booleanPreferencesKey("useAmoledBlackTheme")) { false }

    private val useDynamicColors = persistedPreference(booleanPreferencesKey("useDynamicColors")) { dynamicColorsSupported }

    private val themeSettings = combine(
        theme.flow,
        useAmoledBlackTheme.flow,
        useDynamicColors.flow
    ) { theme, useAmoledBlackTheme, useDynamicColors ->
        ThemeSettings(
            theme = theme,
            useAmoledBlackTheme = useAmoledBlackTheme,
            useDynamicColors = useDynamicColors && dynamicColorsSupported
        )
    }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeSettings(
                theme = Theme.Default,
                useAmoledBlackTheme = false,
                useDynamicColors = dynamicColorsSupported
            )
        )

    private val showStorageVolumeNames = persistedPreference(
        booleanPreferencesKey("showStorageVolumeNames")
    ) { true }

    override val appSettings: StateFlow<AppSettings> = combineStates(
        showStorageVolumeNames.stateIn(scope, SharingStarted.Eagerly),
        themeSettings,
        ::AppSettings
    )

    override val postNotificationsPermissionRequested = persistedPreference(
        booleanPreferencesKey("postNotificationsPermissionRequested")
    ) { false }

    override val showAutoMoveIntroduction = persistedPreference(
        booleanPreferencesKey("showAutoMoveIntroduction")
    ) { true }

    override val showQuickMovePermissionQueryExplanation = persistedPreference(
        booleanPreferencesKey("showQuickMovePermissionQueryExplanation")
    ) { true }

    override suspend fun saveAppSettings(appSettings: AppSettings) {
        edit {
            showStorageVolumeNames setTo appSettings.showStorageVolumeNames
            theme setTo appSettings.theme.theme
            useAmoledBlackTheme setTo appSettings.theme.useAmoledBlackTheme
            useDynamicColors setTo appSettings.theme.useDynamicColors
        }
    }
}
