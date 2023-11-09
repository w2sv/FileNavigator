package com.w2sv.filenavigator.ui.screens.missingpermissions

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PermissionScreen(
    properties: List<PermissionCardProperties>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        items(properties, key = { it.hashCode() }) {
            PermissionCard(
                properties = it,
                modifier = Modifier.animateItemPlacement(
                    tween(
                        DefaultAnimationDuration
                    )
                )
            )
        }
    }
}