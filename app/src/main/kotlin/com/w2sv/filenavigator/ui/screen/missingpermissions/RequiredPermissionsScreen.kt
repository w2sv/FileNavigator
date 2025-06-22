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
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.w2sv.androidutils.openAppSettings
import com.w2sv.common.util.goToManageExternalStorageSettings
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.composed.permissions.extensions.launchPermissionRequest
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.LocalPostNotificationsPermissionState
import com.w2sv.filenavigator.ui.designsystem.TopAppBarAboveHorizontalDivider
import com.w2sv.filenavigator.ui.util.ModifierReceivingComposable
import com.w2sv.filenavigator.ui.util.activityViewModel
import com.w2sv.filenavigator.ui.AppViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

private object RequiredPermissionsScreenDefaults {
    val CardSpacing = 32.dp
}

@Composable
fun RequiredPermissionsScreen(
    postNotificationsPermissionState: PermissionState = LocalPostNotificationsPermissionState.current,
    appViewModel: AppViewModel = activityViewModel()
) {
    val manageAllFilesPermissionGranted by appViewModel.permissions.manageAllFilesGranted.collectAsStateWithLifecycle()

    val permissionCards = rememberMovablePermissionCards(
        manageAllFilesPermissionGranted = manageAllFilesPermissionGranted,
        postNotificationsPermissionRequested = { appViewModel.permissions.postNotificationsGranted.value },
        postNotificationsPermissionState = postNotificationsPermissionState
    )

    Scaffold(topBar = { TopAppBarAboveHorizontalDivider(title = stringResource(id = R.string.required_permissions)) }) { paddingValues ->
        val sharedModifier =
            Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())

        when (isPortraitModeActive) {
            true -> PortraitMode(permissionCards = permissionCards, modifier = sharedModifier)
            false -> LandscapeMode(permissionCards = permissionCards, modifier = sharedModifier)
        }
    }
}

@Composable
private fun PortraitMode(permissionCards: ImmutableList<ModifierReceivingComposable>, modifier: Modifier = Modifier) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(
            RequiredPermissionsScreenDefaults.CardSpacing,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 32.dp)
    ) {
        items(permissionCards) {
            it(Modifier.animateItem())
        }
    }
}

@Composable
private fun LandscapeMode(permissionCards: ImmutableList<ModifierReceivingComposable>, modifier: Modifier = Modifier) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        items(permissionCards) {
            it(
                Modifier
                    .fillMaxWidth(0.4f)
                    .animateItem()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
private fun rememberMovablePermissionCards(
    manageAllFilesPermissionGranted: Boolean,
    postNotificationsPermissionRequested: () -> Boolean,
    postNotificationsPermissionState: PermissionState
): ImmutableList<ModifierReceivingComposable> {
    val context = LocalContext.current
    return remember(
        key1 = postNotificationsPermissionState.status.isGranted,
        key2 = manageAllFilesPermissionGranted
    ) {
        buildList {
            if (!postNotificationsPermissionState.status.isGranted) {
                add(
                    PermissionCardProperties(
                        iconRes = R.drawable.ic_notifications_24,
                        textRes = R.string.post_notifications_permission_rational,
                        onGrantButtonClick = {
                            postNotificationsPermissionState.launchPermissionRequest(
                                launchedBefore = postNotificationsPermissionRequested(),
                                onSuppressed = { context.openAppSettings() }
                            )
                        }
                    )
                )
            }
            if (!manageAllFilesPermissionGranted) {
                add(
                    PermissionCardProperties(
                        iconRes = R.drawable.ic_folder_open_24,
                        textRes = R.string.manage_external_storage_permission_rational,
                        onGrantButtonClick = { goToManageExternalStorageSettings(context) }
                    )
                )
            }
        }
            .map { properties ->
                movableContentOf { mod: Modifier ->
                    PermissionCard(
                        properties = properties,
                        modifier = mod
                    )
                }
            }
            .toPersistentList()
    }
}
