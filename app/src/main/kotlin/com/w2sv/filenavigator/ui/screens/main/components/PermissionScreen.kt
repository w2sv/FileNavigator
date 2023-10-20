package com.w2sv.filenavigator.ui.screens.main.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.PermissionCard
import com.w2sv.filenavigator.ui.components.PermissionCardProperties
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PermissionScreen(
    properties: List<PermissionCardProperties>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.1f))
        AppFontText(
            text = stringResource(id = R.string.permissions_missing),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Start)
        )
        LazyColumn(
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxHeight(0.85f)
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
}