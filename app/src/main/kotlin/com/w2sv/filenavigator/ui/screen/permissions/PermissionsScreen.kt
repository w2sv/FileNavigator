package com.w2sv.filenavigator.ui.screen.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.core.common.R
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.filenavigator.ui.designsystem.TopAppBarAboveHorizontalDivider
import com.w2sv.filenavigator.ui.util.ScreenPreviews
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PermissionsScreen(cards: ImmutableList<PermissionCard>) {
    Scaffold(topBar = { TopAppBarAboveHorizontalDivider(title = stringResource(id = R.string.required_permissions)) }) { paddingValues ->
        val sharedModifier =
            Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())

        when (isPortraitModeActive) {
            true -> PortraitMode(permissionCards = cards, modifier = sharedModifier)
            false -> LandscapeMode(permissionCards = cards, modifier = sharedModifier)
        }
    }
}

@ScreenPreviews
@Composable
private fun Prev() {
    AppTheme {
        PermissionsScreen(
            persistentListOf(
                PermissionCard.postNotifications { },
                PermissionCard.manageAllFiles { }
            )
        )
    }
}

@Composable
private fun PortraitMode(permissionCards: ImmutableList<PermissionCard>, modifier: Modifier = Modifier) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 32.dp)
    ) {
        items(permissionCards, key = { it.hashCode() }) {
            PermissionCard(card = it, modifier = Modifier.animateItem())
        }
    }
}

@Composable
private fun LandscapeMode(permissionCards: ImmutableList<PermissionCard>, modifier: Modifier = Modifier) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        items(permissionCards, key = { it.hashCode() }) {
            PermissionCard(
                card = it,
                modifier = Modifier
                    .fillParentMaxWidth(0.4f)
                    .animateItem()
            )
        }
    }
}
