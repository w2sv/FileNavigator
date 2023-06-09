package com.w2sv.filenavigator.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.filenavigator.ui.NonAppliedSnapshotStateMap
import com.w2sv.filenavigator.ui.NonAppliedState
import com.w2sv.filenavigator.ui.NonAppliedStateFlow
import com.w2sv.filenavigator.ui.NonAppliedStatesComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class AbstractDataStoreRepository(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun <T> save(preferencesKey: Preferences.Key<T>, value: T) {
        dataStore.edit {
            it[preferencesKey] = value
        }
    }

    suspend fun save(
        preferencesKey: Preferences.Key<Int>,
        enum: Enum<*>
    ) {
        dataStore.edit {
            it[preferencesKey] = enum.ordinal
        }
    }

    // ============
    // Maps
    // ============

    protected fun <T, P : DataStoreVariable<T>> mapFromDataStoreProperties(properties: Array<P>): Map<P, Flow<T>> =
        properties.associateWith { property ->
            dataStore.data.map {
                it[property.preferencesKey] ?: property.defaultValue
            }
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
     * Interface for classes interfacing with [dataStoreRepository] via a held [coroutineScope].
     */
    interface Client {

        val dataStoreRepository: AbstractDataStoreRepository
        val coroutineScope: CoroutineScope

        fun <T> saveToDataStore(key: Preferences.Key<T>, value: T) {
            coroutineScope.launch {
                dataStoreRepository.save(key, value)
            }
        }

        fun <T, P : DataStoreVariable<T>> saveMapToDataStore(
            map: Map<P, T>
        ) {
            coroutineScope.launch {
                dataStoreRepository.saveMap(map)
            }
        }
    }

    abstract class InterfacingViewModel<R: AbstractDataStoreRepository>(override val dataStoreRepository: R) :
        ViewModel(),
        Client {

        override val coroutineScope: CoroutineScope get() = viewModelScope

        fun <K : DataStoreVariable<V>, V> makeNonAppliedSnapshotStateMap(appliedFlowMap: Map<K, Flow<V>>): NonAppliedSnapshotStateMap<K, V> =
            NonAppliedSnapshotStateMap(coroutineScope, appliedFlowMap, dataStoreRepository)

        fun <T> makeNonAppliedStateFlow(
            appliedFlow: Flow<T>,
            preferencesKey: Preferences.Key<T>
        ): NonAppliedStateFlow<T> =
            NonAppliedStateFlow(coroutineScope, appliedFlow) {
                dataStoreRepository.save(preferencesKey, it)
            }

        fun makeNonAppliedStatesComposition(vararg nonAppliedState: NonAppliedState<*>): NonAppliedStatesComposition =
            NonAppliedStatesComposition(*nonAppliedState, coroutineScope = coroutineScope)

        fun NonAppliedState<*>.launchSync(): Job =
            coroutineScope.launch {
                sync()
            }

        fun NonAppliedState<*>.launchReset(): Job =
            coroutineScope.launch {
                reset()
            }
    }
}