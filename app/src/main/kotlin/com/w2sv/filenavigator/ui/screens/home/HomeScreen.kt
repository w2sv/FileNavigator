package com.w2sv.filenavigator.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.filenavigator.ui.screens.home.components.movehistory.MoveHistoryCard
import com.w2sv.filenavigator.ui.screens.home.components.statusdisplay.StatusDisplayCard
import com.w2sv.filenavigator.ui.utils.ModifierReceivingComposable
import com.w2sv.filenavigator.ui.utils.rememberMovableContentOf

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    val statusDisplayCard: ModifierReceivingComposable = rememberMovableContentOf {
        StatusDisplayCard(it)
    }
    val moveHistoryCard: ModifierReceivingComposable = rememberMovableContentOf {
        MoveHistoryCard(it)
    }

    if (isPortraitModeActive) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            statusDisplayCard(Modifier.fillMaxHeight(0.28f))
            moveHistoryCard(Modifier.fillMaxHeight(0.8f))
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            statusDisplayCard(
                Modifier
                    .fillMaxWidth(0.4f)
                    .fillMaxHeight(0.8f)
            )
            moveHistoryCard(
                Modifier
                    .fillMaxWidth(0.8f)
            )
        }
    }
}