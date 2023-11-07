package com.w2sv.filenavigator.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.w2sv.filenavigator.ui.screens.home.components.movehistory.MoveHistory
import com.w2sv.filenavigator.ui.screens.home.components.statusdisplay.StatusDisplay

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        StatusDisplay(
            modifier = Modifier
                .fillMaxHeight(0.28f)
        )
        MoveHistory(
            modifier = Modifier.fillMaxHeight(0.8f)
        )
    }
}