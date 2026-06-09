package com.w2sv.filenavigator.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.designsystem.layout.PaddingDefaults
import com.w2sv.filenavigator.ui.screen.home.movehistory.MoveHistoryCard
import com.w2sv.filenavigator.ui.screen.home.movehistory.MoveHistoryState
import com.w2sv.filenavigator.ui.screen.home.navigatorstatus.NavigatorStatusCard
import com.w2sv.filenavigator.ui.shared.debugging.PreviewOf
import com.w2sv.filenavigator.ui.shared.util.ModifierReceivingComposable
import com.w2sv.filenavigator.ui.shared.util.rememberMovableContentOf
import kotlinx.collections.immutable.persistentListOf

@Composable
fun HomeScreen(navigatorIsRunning: Boolean, moveHistoryState: MoveHistoryState) {
    val sharedModifier = Modifier.fillMaxSize()

    val statusDisplayCard: ModifierReceivingComposable = rememberMovableContentOf {
        NavigatorStatusCard(navigatorIsRunning = navigatorIsRunning, modifier = it)
    }
    val moveHistoryCard: ModifierReceivingComposable = rememberMovableContentOf {
        MoveHistoryCard(state = moveHistoryState, modifier = it)
    }

    when (isPortraitModeActive) {
        true -> PortraitMode(
            statusDisplayCard = statusDisplayCard,
            moveHistoryCard = moveHistoryCard,
            modifier = sharedModifier
        )

        false -> LandscapeMode(
            statusDisplayCard = statusDisplayCard,
            moveHistoryCard = moveHistoryCard,
            modifier = sharedModifier
        )
    }
}

@Preview
@Composable
private fun HomeScreenPrev() {
    PreviewOf {
        HomeScreen(
            navigatorIsRunning = true,
            moveHistoryState = MoveHistoryState(
                history = persistentListOf(),
                deleteAll = {},
                deleteEntry = {}
            )
        )
    }
}

@Composable
private fun PortraitMode(
    statusDisplayCard: ModifierReceivingComposable,
    moveHistoryCard: ModifierReceivingComposable,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = PaddingDefaults.horizontal),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        statusDisplayCard(Modifier)
        moveHistoryCard(Modifier.fillMaxHeight(0.85f))
    }
}

@Composable
private fun LandscapeMode(
    statusDisplayCard: ModifierReceivingComposable,
    moveHistoryCard: ModifierReceivingComposable,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        statusDisplayCard(
            Modifier
                .fillMaxWidth(0.45f)
        )
        moveHistoryCard(
            Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f)
        )
    }
}
