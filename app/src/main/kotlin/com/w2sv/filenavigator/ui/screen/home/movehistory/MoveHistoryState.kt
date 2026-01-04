package com.w2sv.filenavigator.ui.screen.home.movehistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.MovedFile
import com.w2sv.filenavigator.ui.screen.home.HomeScreenViewModel
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Stable
data class MoveHistoryState(val history: ImmutableList<MovedFile>, val deleteAll: () -> Unit, val deleteEntry: (MovedFile) -> Unit) {
    val historyEmpty by threadUnsafeLazy { history.isEmpty() }

    companion object {
        @Composable
        fun remember(viewModel: HomeScreenViewModel): MoveHistoryState {
            val history by viewModel.moveHistory.collectAsStateWithLifecycle()

            return androidx.compose.runtime.remember(history, viewModel) {
                MoveHistoryState(
                    history = history.toImmutableList(),
                    deleteAll = viewModel::launchHistoryDeletion,
                    deleteEntry = viewModel::launchEntryDeletion
                )
            }
        }
    }
}
