package com.w2sv.filenavigator.ui.sharedviewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.interfaces.MoveEntryRepository
import com.w2sv.domain.model.MoveEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoveHistoryViewModel @Inject constructor(private val moveEntryRepository: MoveEntryRepository) :
    ViewModel() {

    val moveHistory = moveEntryRepository
        .getAllInDescendingOrder()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun launchHistoryDeletion(): Job =
        viewModelScope.launch(Dispatchers.IO) {
            moveEntryRepository.deleteAll()
        }

    fun launchEntryDeletion(entry: MoveEntry): Job =
        viewModelScope.launch(Dispatchers.IO) {
            moveEntryRepository.delete(entry)
        }
}