package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

object AppCardDefaults {
    val elevation
        @Composable
        get() = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
}
