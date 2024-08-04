package com.w2sv.filenavigator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.MoveEntry
import com.w2sv.domain.repository.MoveEntryRepository
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun launchHistoryDeletion(): Job =
        viewModelScope.launch(Dispatchers.IO) {
            moveEntryRepository.deleteAll()
        }

    fun launchEntryDeletion(entry: MoveEntry): Job =
        viewModelScope.launch(Dispatchers.IO) {
            moveEntryRepository.delete(entry)
        }
}