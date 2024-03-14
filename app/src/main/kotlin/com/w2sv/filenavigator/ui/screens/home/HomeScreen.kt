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

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    if (isPortraitModeActive) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusDisplayCard(
                modifier = Modifier
                    .fillMaxHeight(0.28f)
            )
            MoveHistoryCard(
                modifier = Modifier.fillMaxHeight(0.8f)
            )
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusDisplayCard(modifier = Modifier
                .fillMaxWidth(0.4f)
                .fillMaxHeight(0.8f))
            MoveHistoryCard(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            )
        }
    }
}