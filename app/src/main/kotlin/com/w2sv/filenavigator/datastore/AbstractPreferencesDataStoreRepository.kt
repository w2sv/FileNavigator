package com.w2sv.filenavigator.datastore

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.w2sv.kotlinutils.extensions.getByOrdinal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class AbstractPreferencesDataStoreRepository(
    protected val dataStore: DataStore<Preferences>
) {

    // ================
    // Simple values
    // ================

    protected fun <T> getFlow(preferencesKey: Preferences.Key<T>, defaultValue: T): Flow<T> =
        dataStore.data.map {
            it[preferencesKey] ?: defaultValue
        }

    suspend fun <T> save(preferencesKey: Preferences.Key<T>, value: T) {
        dataStore.edit {
            it[preferencesKey] = value
        }
    }

    // ================
    // URIs
    // ================

    fun getUriFlow(
        preferencesKey: Preferences.Key<String>,
        defaultValue: Uri?
    ): Flow<Uri?> =
        dataStore.data.map {
            it[preferencesKey]?.let { string -> if (string.isEmpty()) null else Uri.parse(string) }
                ?: defaultValue
        }

    fun getUriFlow(entry: DataStoreEntry.UriValued): Flow<Uri?> =
        getUriFlow(entry.preferencesKey, entry.defaultValue)

    suspend fun save(preferencesKey: Preferences.Key<String>, value: Uri?) {
        dataStore.edit {
            it[preferencesKey] = value?.toString() ?: ""
        }
    }

    // ============
    // Enums
    // ============

    protected inline fun <reified T : Enum<T>> getEnumFlow(
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
    // Simple Maps
    // ============

    protected fun <T, DSE : DataStoreEntry.UniType<T>> getFlowMap(properties: Iterable<DSE>): Map<DSE, Flow<T>> =
        properties.associateWith { property ->
            getFlow(property.preferencesKey, property.defaultValue)
        }

    suspend fun <V, K : DataStoreEntry.UniType<V>> saveMap(
        map: Map<K, V>
    ) {
        dataStore.edit {
            map.forEach { (entry, value) ->
                save(entry.preferencesKey, value)
            }
        }
    }

    // ============
    // UriValued Maps
    // ============

    protected fun <DSE : DataStoreEntry.UriValued> getUriFlowMap(entries: Iterable<DSE>): Map<DSE, Flow<Uri?>> =
        entries.associateWith {
            getUriFlow(it.preferencesKey, it.defaultValue)
        }

    suspend fun <DSE : DataStoreEntry.UriValued> saveMap(map: Map<DSE, Uri?>) {
        dataStore.edit {
            map.forEach { (entry, value) ->
                save(entry.preferencesKey, value)
            }
        }
    }

    // ============
    // EnumValued Maps
    // ============

    protected inline fun <reified V : Enum<V>, K : DataStoreEntry.EnumValued<V>> getEnumValuedFlowMap(
        properties: Iterable<K>
    ): Map<K, Flow<V>> =
        properties.associateWith { property ->
            getEnumFlow(property.preferencesKey, property.defaultValue)
        }

    suspend fun <V : Enum<V>, K : DataStoreEntry.EnumValued<V>> saveMap(
        map: Map<K, V>
    ) {
        dataStore.edit {
            map.forEach { (entry, value) ->
                save(entry.preferencesKey, value)
            }
        }
    }

    /**
     * Interface for classes interfacing with a [AbstractPreferencesDataStoreRepository] via a held [coroutineScope].
     */
    interface Interface {
        val dataStoreRepository: AbstractPreferencesDataStoreRepository
        val coroutineScope: CoroutineScope

        fun <T> saveToDataStore(key: Preferences.Key<T>, value: T): Job =
            coroutineScope.launch(Dispatchers.IO) {
                dataStoreRepository.save(key, value)
            }

        fun saveToDataStore(key: Preferences.Key<String>, value: Uri?): Job =
            coroutineScope.launch(Dispatchers.IO) {
                dataStoreRepository.save(key, value)
            }

        fun <T, P : DataStoreEntry.UniType<T>> saveMapToDataStore(
            map: Map<P, T>
        ): Job =
            coroutineScope.launch(Dispatchers.IO) {
                dataStoreRepository.saveMap(map)
            }
    }

    abstract class ViewModel<R : AbstractPreferencesDataStoreRepository>(override val dataStoreRepository: R) :
        androidx.lifecycle.ViewModel(),
        Interface {

        override val coroutineScope: CoroutineScope by ::viewModelScope
    }
}