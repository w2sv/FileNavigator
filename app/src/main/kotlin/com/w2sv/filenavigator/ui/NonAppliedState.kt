package com.w2sv.filenavigator.ui

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.filenavigator.datastore.AbstractDataStoreRepository
import com.w2sv.filenavigator.datastore.DataStoreVariable
import com.w2sv.filenavigator.datastore.DataStoreRepository
import com.w2sv.filenavigator.utils.getMutableStateMap
import com.w2sv.filenavigator.utils.getSynchronousMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

abstract class NonAppliedState<T> {
    val stateChanged = MutableStateFlow(false)

    protected fun MutableStateFlow<Boolean>.reset() {
        value = false
    }

    abstract suspend fun sync()
    abstract suspend fun reset()
}

class NonAppliedSnapshotStateMap<K : DataStoreVariable<V>, V>(
    private val coroutineScope: CoroutineScope,
    private val appliedFlowMap: Map<K, Flow<V>>,
    private val dataStoreRepository: AbstractDataStoreRepository,
    private val map: SnapshotStateMap<K, V> = appliedFlowMap
        .getSynchronousMap()
        .getMutableStateMap(),
    private val onStateSynced: (Map<K, V>) -> Unit = {}
) : NonAppliedState<Map<K, V>>(),
    MutableMap<K, V> by map {

    private val dissimilarKeys = mutableSetOf<K>()

    private val dissimilarEntries: Map<K, V>
        get() = filterKeys { it in dissimilarKeys }

    override fun put(key: K, value: V): V? =
        map.put(key, value)
            .also {
                coroutineScope.launch {
                    when (value == appliedFlowMap.getValue(key).first()) {
                        true -> dissimilarKeys.remove(key)
                        false -> dissimilarKeys.add(key)
                    }
                    stateChanged.value = dissimilarKeys.isNotEmpty()
                }
            }

    override suspend fun sync() {
        dataStoreRepository.saveMap(dissimilarEntries)
        onStateSynced(this)
        resetInternally()
    }

    override suspend fun reset() {
        dissimilarKeys
            .forEach {
                map[it] = appliedFlowMap.getValue(it).first()
            }
        resetInternally()
    }

    private fun resetInternally() {
        dissimilarKeys.clear()
        stateChanged.reset()
    }
}

class NonAppliedStateFlow<T>(
    coroutineScope: CoroutineScope,
    private val appliedFlow: Flow<T>,
    private val syncState: suspend (T) -> Unit
) : NonAppliedState<T>(),
    MutableStateFlow<T> by MutableStateFlow(
        appliedFlow.getValueSynchronously()
    ) {

    init {
        coroutineScope.launch {
            collect {
                stateChanged.value = it != appliedFlow.first()
            }
        }
    }

    override suspend fun sync() {
        syncState(value)
        stateChanged.reset()
    }

    override suspend fun reset() {
        value = appliedFlow.first()
    }
}

class NonAppliedStatesComposition(
    vararg nonAppliedState: NonAppliedState<*>,
    coroutineScope: CoroutineScope
) : List<NonAppliedState<*>> by nonAppliedState.asList(),
    NonAppliedState<List<NonAppliedState<*>>>() {

    private val changedStateIndices = mutableSetOf<Int>()

    init {
        coroutineScope.launch {
            mapIndexed { i, it -> it.stateChanged.transform { emit(it to i) } }
                .merge()
                .collect { (stateChanged, i) ->
                    if (stateChanged) {
                        changedStateIndices.add(i)
                    } else {
                        changedStateIndices.remove(i)
                    }

                    this@NonAppliedStatesComposition.stateChanged.value =
                        changedStateIndices.isNotEmpty()
                }
        }
    }

    override suspend fun sync() {
        forEach {
            if (it.stateChanged.value) {
                it.sync()
            }
        }
    }

    override suspend fun reset() {
        forEach {
            if (it.stateChanged.value) {
                it.reset()
            }
        }
    }
}