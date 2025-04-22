package com.w2sv.filenavigator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.repository.MovedFileRepository
import com.w2sv.domain.usecase.GetMoveHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MoveHistoryViewModel @Inject constructor(
    private val movedFileRepository: MovedFileRepository,
    getMoveHistoryUseCase: GetMoveHistoryUseCase
) :
    ViewModel() {

    val moveHistory = getMoveHistoryUseCase
        .invoke()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun launchHistoryDeletion(): Job =
        viewModelScope.launch(Dispatchers.IO) {
            movedFileRepository.deleteAll()
        }

    fun launchEntryDeletion(movedFile: MovedFile): Job =
        viewModelScope.launch(Dispatchers.IO) {
            movedFileRepository.delete(movedFile)
        }
}
