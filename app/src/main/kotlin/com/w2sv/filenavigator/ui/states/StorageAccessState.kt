package com.w2sv.filenavigator.ui.states

import android.content.Context
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.androidutils.datastorage.datastore.preferences.PersistedValue
import com.w2sv.data.model.StorageAccessStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import slimber.log.i

class StorageAccessState(
    private val priorStorageAccessStatus: PersistedValue.EnumValued<StorageAccessStatus>,
    private val scope: CoroutineScope
) {
    private val _status = MutableStateFlow(priorStorageAccessStatus.getValueSynchronously())

    val anyAccessGranted: StateFlow<Boolean> =
        _status.mapState { it != StorageAccessStatus.NoAccess }

    /**
     * @return [StorageAccessStatus] if distinct from previous one, otherwise null.
     */
    fun updateStatus(context: Context): StorageAccessStatus? =
        StorageAccessStatus.get(context).let { newStatus ->
            if (newStatus != _status.value) {
                _status.value = newStatus
                scope.launch {
                    priorStorageAccessStatus.save(newStatus)
                }
                newStatus.also { i { "New status=$newStatus" } }
            } else {
                null
            }
        }
}