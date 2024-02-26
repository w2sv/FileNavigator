package com.w2sv.filenavigator.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.w2sv.filenavigator.ui.screens.home.components.movehistory.MoveHistoryCard
import com.w2sv.filenavigator.ui.screens.home.components.statusdisplay.StatusDisplayCard

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
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
}