package com.w2sv.filenavigator.ui

import androidx.datastore.preferences.core.Preferences
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.filenavigator.datastore.DataStoreEntry
import com.w2sv.filenavigator.datastore.AbstractPreferencesDataStoreRepository
import com.w2sv.filenavigator.utils.getMutableStateMap
import com.w2sv.filenavigator.utils.getSynchronousMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import slimber.log.i

/**
 * Base class for classes, encapsulating states being displayed by the UI but pending
 * confirmation, which in turn triggers the synchronization with the respective repository.
 */
abstract class UnconfirmedState<T> {
    val statesDissimilar: StateFlow<Boolean> get() = _statesDissimilar
    protected val _statesDissimilar = MutableStateFlow(false)

    protected val logIdentifier: String get() = this::class.java.simpleName

    abstract suspend fun sync()
    abstract suspend fun reset()
}

class UnconfirmedStateMap<K : DataStoreEntry<V1, V2>, V1, V2>(
    private val coroutineScope: CoroutineScope,
    private val appliedFlowMap: Map<K, Flow<V1>>,
    private val map: MutableMap<K, V1> = appliedFlowMap
        .getSynchronousMap()
        .getMutableStateMap(),
    private val syncState: suspend (Map<K, V1>) -> Unit
) : UnconfirmedState<Map<K, V1>>(),
    MutableMap<K, V1> by map {

    /**
     * Tracking of keys which correspond to values, differing between [appliedFlowMap] and this
     * for efficient syncing/resetting.
     */
    private val dissimilarKeys = mutableSetOf<K>()

    // ==============
    // Modification
    // ==============

    /**
     * Inherently updates [dissimilarKeys] and [statesDissimilar] in an asynchronous fashion.
     */
    override fun put(key: K, value: V1): V1? =
        map.put(key, value)
            .also {
                coroutineScope.launch {
                    when (value == appliedFlowMap.getValue(key).first()) {
                        true -> dissimilarKeys.remove(key)
                        false -> dissimilarKeys.add(key)
                    }
                    _statesDissimilar.value = dissimilarKeys.isNotEmpty()
                }
            }

    override fun putAll(from: Map<out K, V1>) {
        from.forEach { (k, v) ->
            put(k, v)
        }
    }

    // =======================
    // Syncing / Resetting
    // =======================

    override suspend fun sync() = withSubsequentInternalReset {
        i { "Syncing $logIdentifier" }

        syncState(filterKeys { it in dissimilarKeys })
    }

    override suspend fun reset() = withSubsequentInternalReset {
        dissimilarKeys
            .forEach {
                // Call map.put directly to prevent unnecessary state updates
                map[it] = appliedFlowMap.getValue(it).first()
            }
    }

    private inline fun withSubsequentInternalReset(f: () -> Unit) {
        f()

        dissimilarKeys.clear()
        _statesDissimilar.value = false
    }
}

class UnconfirmedStateFlow<T>(
    coroutineScope: CoroutineScope,
    private val appliedFlow: Flow<T>,
    initialValue: T = appliedFlow.getValueSynchronously(),
    private val syncState: suspend (T) -> Unit
) : UnconfirmedState<T>(),
    MutableStateFlow<T> by MutableStateFlow(initialValue) {

    init {
        // Update [statesDissimilar] whenever a new value is collected
        coroutineScope.launch {
            collect { newValue ->
                _statesDissimilar.value = newValue != appliedFlow.first()
            }
        }
    }

    override suspend fun sync() {
        i { "Syncing $logIdentifier" }

        syncState(value)
        _statesDissimilar.value = false
    }

    override suspend fun reset() {
        i { "Resetting $logIdentifier" }

        value = appliedFlow.first()  // Triggers [statesDissimilar] updating flow collector anyways
    }
}

typealias UnconfirmedStates = List<UnconfirmedState<*>>

class UnconfirmedStatesComposition(
    unconfirmedStates: UnconfirmedStates,
    coroutineScope: CoroutineScope
) : UnconfirmedStates by unconfirmedStates,
    UnconfirmedState<UnconfirmedStates>() {

    private val changedStateInstanceIndices = mutableSetOf<Int>()
    private val changedStateInstances get() = changedStateInstanceIndices.map { this[it] }

    init {
        // Update [changedStateInstanceIndices] and [_statesDissimilar] upon change of one of
        // the held element's [statesDissimilar]
        coroutineScope.launch {
            mapIndexed { i, it -> it.statesDissimilar.transform { emit(it to i) } }
                .merge()
                .collect { (statesDissimilar, i) ->
                    if (statesDissimilar) {
                        changedStateInstanceIndices.add(i)
                    } else {
                        changedStateInstanceIndices.remove(i)
                    }

                    _statesDissimilar.value = changedStateInstanceIndices.isNotEmpty()
                }
        }
    }

    override suspend fun sync() {
        i { "Syncing $logIdentifier" }

        changedStateInstances.forEach {
            it.sync()
        }
    }

    override suspend fun reset() {
        i { "Resetting $logIdentifier" }

        changedStateInstances.forEach {
            it.reset()
        }
    }
}

abstract class UnconfirmedStatesHoldingViewModel<R : AbstractPreferencesDataStoreRepository>(
    dataStoreRepository: R
) : AbstractPreferencesDataStoreRepository.ViewModel<R>(dataStoreRepository) {

    // =======================
    // Instance creation
    // =======================

    fun <K : DataStoreEntry.UniType<V>, V> makeUnconfirmedStateMap(appliedFlowMap: Map<K, Flow<V>>): UnconfirmedStateMap<K, V, V> =
        UnconfirmedStateMap(
            coroutineScope,
            appliedFlowMap,
            syncState = { dataStoreRepository.saveMap(it) }
        )

    fun <K : DataStoreEntry.EnumValued<V>, V : Enum<V>> makeUnconfirmedEnumValuedStateMap(
        appliedFlowMap: Map<K, Flow<V>>
    ): UnconfirmedStateMap<K, V, Int> =
        UnconfirmedStateMap(
            coroutineScope,
            appliedFlowMap,
            syncState = { dataStoreRepository.saveEnumValuedMap(it) }
        )

    fun <T> makeUnconfirmedStateFlow(
        appliedFlow: Flow<T>,
        preferencesKey: Preferences.Key<T>
    ): UnconfirmedStateFlow<T> =
        UnconfirmedStateFlow(coroutineScope, appliedFlow) {
            dataStoreRepository.save(preferencesKey, it)
        }

    inline fun <reified T : Enum<T>> makeUnconfirmedEnumStateFlow(
        appliedFlow: Flow<T>,
        preferencesKey: Preferences.Key<Int>
    ): UnconfirmedStateFlow<T> =
        UnconfirmedStateFlow(coroutineScope, appliedFlow) {
            dataStoreRepository.save(preferencesKey, it)
        }

    fun makeUnconfirmedStatesComposition(unconfirmedStates: UnconfirmedStates): UnconfirmedStatesComposition =
        UnconfirmedStatesComposition(unconfirmedStates, coroutineScope = coroutineScope)

    // =======================
    // Syncing / resetting on coroutine scope
    // =======================

    fun UnconfirmedState<*>.launchSync(): Job =
        coroutineScope.launch {
            sync()
        }

    fun UnconfirmedState<*>.launchReset(): Job =
        coroutineScope.launch {
            reset()
        }
}