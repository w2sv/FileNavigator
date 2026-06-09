package com.w2sv.filenavigator.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.w2sv.common.logging.LoggingViewModel
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.repository.MovedFileRepository
import com.w2sv.domain.usecase.GetMoveHistoryUseCase
import com.w2sv.navigator.di.FileNavigatorIsRunning
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val movedFileRepository: MovedFileRepository,
    getMoveHistoryUseCase: GetMoveHistoryUseCase,
    @FileNavigatorIsRunning val navigatorIsRunning: StateFlow<Boolean>
) : LoggingViewModel() {

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
