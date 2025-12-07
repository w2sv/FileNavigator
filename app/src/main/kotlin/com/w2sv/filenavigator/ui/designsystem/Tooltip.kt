package com.w2sv.filenavigator.ui.designsystem

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@SuppressLint("ComposeUnstableReceiver")
@Composable
fun TooltipScope.DeletionTooltip(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    PlainTooltip(tonalElevation = 4.dp, shadowElevation = 4.dp, modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = contentDescription
            )
        }
    }
}
