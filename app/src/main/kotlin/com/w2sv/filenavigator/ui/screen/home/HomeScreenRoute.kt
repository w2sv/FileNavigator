package com.w2sv.filenavigator.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.filenavigator.ui.screen.home.movehistory.MoveHistoryState

@Composable
fun HomeScreenRoute(viewModel: HomeScreenViewModel = hiltViewModel()) {
    val navigatorIsRunning by viewModel.navigatorIsRunning.collectAsStateWithLifecycle()
    val moveHistoryState = MoveHistoryState.remember(viewModel)

    HomeScreen(navigatorIsRunning = navigatorIsRunning, moveHistoryState = moveHistoryState)
}
