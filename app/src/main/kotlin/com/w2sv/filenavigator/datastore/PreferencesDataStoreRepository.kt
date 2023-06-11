package com.w2sv.filenavigator.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.w2sv.kotlinutils.extensions.getByOrdinal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class PreferencesDataStoreRepository(
    protected val dataStore: DataStore<Preferences>
) {

    protected fun <T> getFlow(preferencesKey: Preferences.Key<T>, defaultValue: T): Flow<T> =
        dataStore.data.map {
            it[preferencesKey] ?: defaultValue
        }

    suspend fun <T> save(preferencesKey: Preferences.Key<T>, value: T) {
        dataStore.edit {
            it[preferencesKey] = value
        }
    }

    // ============
    // Enums
    // ============

    protected inline fun <reified T : Enum<T>> getEnumFLow(
        preferencesKey: Preferences.Key<Int>,
        defaultValue: T
    ): Flow<T> =
        dataStore.data.map {
            it[preferencesKey]
                ?.let { ordinal -> getByOrdinal<T>(ordinal) }
                ?: defaultValue
        }

    suspend fun save(
        preferencesKey: Preferences.Key<Int>,
        enum: Enum<*>
    ) {
        save(preferencesKey, enum.ordinal)
    }

    // ============
    // Maps
    // ============

    protected fun <T, P : DataStoreVariable<T>> getFlowMap(properties: Iterable<P>): Map<P, Flow<T>> =
        properties.associateWith { property ->
            getFlow(property.preferencesKey, property.defaultValue)
        }

    suspend fun <T, P : DataStoreVariable<T>> saveMap(
        map: Map<P, T>
    ) {
        dataStore.edit {
            map.forEach { (property, value) ->
                it[property.preferencesKey] = value
            }
        }
    }

    /**
     * Interface for classes interfacing with a [PreferencesDataStoreRepository] via a held [coroutineScope].
     */
    interface Interface {
        val repository: PreferencesDataStoreRepository
        val coroutineScope: CoroutineScope

        fun <T> saveToDataStore(key: Preferences.Key<T>, value: T) {
            coroutineScope.launch {
                repository.save(key, value)
            }
        }

        fun <T, P : DataStoreVariable<T>> saveMapToDataStore(
            map: Map<P, T>
        ) {
            coroutineScope.launch {
                repository.saveMap(map)
            }
        }
    }

    abstract class ViewModel<R : PreferencesDataStoreRepository>(override val repository: R) :
        androidx.lifecycle.ViewModel(),
        Interface {

        override val coroutineScope: CoroutineScope get() = viewModelScope
    }
}