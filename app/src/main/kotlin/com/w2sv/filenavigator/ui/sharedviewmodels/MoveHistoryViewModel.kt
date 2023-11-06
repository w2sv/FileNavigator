package com.w2sv.filenavigator.ui.sharedviewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.data.storage.database.MoveEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoveHistoryViewModel @Inject constructor(private val moveEntryDao: MoveEntryDao) :
    ViewModel() {

    val moveHistory = moveEntryDao.loadAllInDescendingOrder()

    fun launchHistoryDeletion(): Job =
        viewModelScope.launch(Dispatchers.IO) {
            moveEntryDao.deleteAll()
        }
}