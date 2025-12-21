package com.w2sv.filenavigator.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.ui.designsystem.AppCardDefaults

@Composable
fun HomeScreenCard(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        elevation = AppCardDefaults.elevation
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}
