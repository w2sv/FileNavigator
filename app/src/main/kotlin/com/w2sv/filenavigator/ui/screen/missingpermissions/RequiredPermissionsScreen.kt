package com.w2sv.filenavigator.ui.screen.missingpermissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.w2sv.androidutils.openAppSettings
import com.w2sv.common.util.goToManageExternalStorageSettings
import com.w2sv.composed.core.OnChange
import com.w2sv.composed.core.isPortraitModeActive
import com.w2sv.composed.permissions.extensions.launchPermissionRequest
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.AppViewModel
import com.w2sv.filenavigator.ui.designsystem.TopAppBarAboveHorizontalDivider
import com.w2sv.filenavigator.ui.sharedstate.AppPermissionsState
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.activityViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

private object RequiredPermissionsScreenDefaults {
    val CardSpacing = 32.dp
}

@Composable
fun RequiredPermissionsScreen(appVM: AppViewModel = activityViewModel()) {
    val permissionsState = appVM.permissionsState
    val postNotificationsPermissionState = rememberPostNotificationsPermissionState()

    SyncPostNotificationsPermissionState(
        permissionState = postNotificationsPermissionState,
        appPermissionsState = permissionsState
    )

    val postNotificationsPermissionGranted by permissionsState.postNotificationsPermissionGranted.collectAsStateWithLifecycle()
    val manageAllFilesPermissionGranted by permissionsState.manageAllFilesPermissionGranted.collectAsStateWithLifecycle()

    val permissionCards = remember(
        postNotificationsPermissionGranted,
        manageAllFilesPermissionGranted
    ) {
        buildPermissionCards(
            postNotificationsPermissionGranted = postNotificationsPermissionGranted,
            manageAllFilesPermissionGranted = manageAllFilesPermissionGranted,
            permissionsState = permissionsState,
            postNotificationsPermissionState = postNotificationsPermissionState
        )
    }

    RequiredPermissionsScreen(cards = permissionCards)
}

@Composable
private fun SyncPostNotificationsPermissionState(permissionState: PermissionState, appPermissionsState: AppPermissionsState) {
    OnChange(permissionState.status) { status ->
        appPermissionsState.setPostNotificationsPermissionGranted(status.isGranted)
    }
}

private fun buildPermissionCards(
    postNotificationsPermissionGranted: Boolean,
    manageAllFilesPermissionGranted: Boolean,
    permissionsState: AppPermissionsState,
    postNotificationsPermissionState: PermissionState
): ImmutableList<PermissionCard> =
    buildList {
        if (!postNotificationsPermissionGranted) {
            add(
                PermissionCard.postNotifications(
                    onGrantButtonClick = { context ->
                        permissionsState.onPostNotificationsPermissionRequested()
                        postNotificationsPermissionState.launchPermissionRequest(
                            launchedBefore = permissionsState.postNotificationsPermissionRequested(),
                            onSuppressed = { context.openAppSettings() }
                        )
                    }
                )
            )
        }
        if (!manageAllFilesPermissionGranted) {
            add(PermissionCard.manageAllFiles(onGrantButtonClick = { context -> goToManageExternalStorageSettings(context) }))
        }
    }.toPersistentList()

@Composable
private fun RequiredPermissionsScreen(cards: ImmutableList<PermissionCard>) {
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

@Preview
@Composable
private fun Prev() {
    AppTheme {
        RequiredPermissionsScreen(
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
        verticalArrangement = Arrangement.spacedBy(
            RequiredPermissionsScreenDefaults.CardSpacing,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 32.dp)
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
                    .fillMaxWidth(0.4f)
                    .animateItem()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}
