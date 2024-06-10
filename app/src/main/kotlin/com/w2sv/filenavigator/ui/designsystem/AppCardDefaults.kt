package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object AppCardDefaults {
    val moreElevatedCardElevation
        @Composable
        get() = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
}