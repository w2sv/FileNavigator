package com.w2sv.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.datastoreutils.datastoreflow.DataStoreStateFlow
import com.w2sv.datastoreutils.preferences.PreferencesDataStoreRepository
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Singleton
internal class PreferencesRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>,
    @param:GlobalScope(AppDispatcher.Default) private val defaultScope: CoroutineScope
) : PreferencesDataStoreRepository(dataStore),
    PreferencesRepository {

    override val theme = enumDataStoreFlow(
        intPreferencesKey("theme")
    ) { Theme.Default }

    override val useAmoledBlackTheme =
        dataStoreFlow(booleanPreferencesKey("useAmoledBlackTheme")) { false }

    override val useDynamicColors = dataStoreFlow(
        booleanPreferencesKey("useDynamicColors")
    ) { dynamicColorsSupported }

    override val postNotificationsPermissionRequested = dataStoreFlow(
        booleanPreferencesKey("postNotificationsPermissionRequested")
    ) { false }

    override val showStorageVolumeNames: DataStoreFlow<Boolean> = dataStoreFlow(
        booleanPreferencesKey("showStorageVolumeNames")
    ) { true }

    override val showAutoMoveIntroduction: DataStoreFlow<Boolean> = dataStoreFlow(
        booleanPreferencesKey("showAutoMoveIntroduction")
    ) { true }

    override val showQuickMovePermissionQueryExplanation: DataStoreStateFlow<Boolean> by lazy {
        dataStoreFlow(booleanPreferencesKey("showQuickMovePermissionQueryExplanation")) { true }
            .stateInWithSynchronousInitial(defaultScope)
    }
}
