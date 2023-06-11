package com.w2sv.filenavigator.ui

import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.filenavigator.datastore.AbstractDataStoreRepository
import com.w2sv.filenavigator.datastore.DataStoreVariable
import com.w2sv.filenavigator.utils.getMutableStateMap
import com.w2sv.filenavigator.utils.getSynchronousMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

/**
 * Base class for classes, encapsulating states being displayed by the UI but pending
 * confirmation, which in turn triggers the synchronization with the respective repository.
 */
abstract class UnconfirmedState<T> {
    val statesDissimilar = MutableStateFlow(false)

    abstract suspend fun sync()
    abstract suspend fun reset()
}

class UnconfirmedStateMap<K : DataStoreVariable<V>, V>(
    private val coroutineScope: CoroutineScope,
    private val appliedFlowMap: Map<K, Flow<V>>,
    private val dataStoreRepository: AbstractDataStoreRepository,
    private val map: MutableMap<K, V> = appliedFlowMap
        .getSynchronousMap()
        .getMutableStateMap(),
    private val onStateSyncedListener: (Map<K, V>) -> Unit = {}
) : UnconfirmedState<Map<K, V>>(),
    MutableMap<K, V> by map {

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
    override fun put(key: K, value: V): V? =
        map.put(key, value)
            .also {
                coroutineScope.launch {
                    when (value == appliedFlowMap.getValue(key).first()) {
                        true -> dissimilarKeys.remove(key)
                        false -> dissimilarKeys.add(key)
                    }
                    statesDissimilar.value = dissimilarKeys.isNotEmpty()
                }
            }

    override fun putAll(from: Map<out K, V>) {
        from.forEach { (k, v) ->
            put(k, v)
        }
    }

    // =======================
    // Syncing / Resetting
    // =======================

    override suspend fun sync() = withSubsequentInternalReset {
        dataStoreRepository.saveMap(filterKeys { it in dissimilarKeys })
        onStateSyncedListener(this)
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
        statesDissimilar.value = false
    }
}

class UnconfirmedStateFlow<T>(
    coroutineScope: CoroutineScope,
    private val appliedFlow: Flow<T>,
    private val syncState: suspend (T) -> Unit
) : UnconfirmedState<T>(),
    MutableStateFlow<T> by MutableStateFlow(
        appliedFlow.getValueSynchronously()
    ) {

    init {
        // Update [statesDissimilar] whenever a new value is collected
        coroutineScope.launch {
            collect { newValue ->
                statesDissimilar.value = newValue != appliedFlow.first()
            }
        }
    }

    override suspend fun sync() {
        syncState(value)
        statesDissimilar.value = false
    }

    override suspend fun reset() {
        value = appliedFlow.first()  // Triggers [statesDissimilar] updating flow collector anyways
    }
}

class UnconfirmedStatesComposition(
    vararg unconfirmedState: UnconfirmedState<*>,
    coroutineScope: CoroutineScope
) : List<UnconfirmedState<*>> by unconfirmedState.asList(),
    UnconfirmedState<List<UnconfirmedState<*>>>() {

    private val changedStateIndices = mutableSetOf<Int>()

    init {
        coroutineScope.launch {
            mapIndexed { i, it -> it.statesDissimilar.transform { emit(it to i) } }
                .merge()
                .collect { (stateChanged, i) ->
                    if (stateChanged) {
                        changedStateIndices.add(i)
                    } else {
                        changedStateIndices.remove(i)
                    }

                    this@UnconfirmedStatesComposition.statesDissimilar.value =
                        changedStateIndices.isNotEmpty()
                }
        }
    }

    override suspend fun sync() {
        forEach {
            if (it.statesDissimilar.value) {
                it.sync()
            }
        }
    }

    override suspend fun reset() {
        forEach {
            if (it.statesDissimilar.value) {
                it.reset()
            }
        }
    }
}