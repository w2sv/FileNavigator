package com.w2sv.filenavigator.ui.screens.missingpermissions

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.composed.OnChange
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.composed.permissions.extensions.launchPermissionRequest
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.NavigationTransitions
import com.w2sv.filenavigator.ui.designsystem.TopAppBarAboveHorizontalDivider
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.states.PostNotificationsPermissionState
import com.w2sv.filenavigator.ui.utils.ModifierReceivingComposable
import com.w2sv.filenavigator.ui.utils.activityViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Destination<RootGraph>(style = NavigationTransitions::class)
@Composable
fun RequiredPermissionsScreen(
    postNotificationsPermissionState: PostNotificationsPermissionState,
    navigator: DestinationsNavigator
) {
    val permissionCards =
        rememberMovablePermissionCards(postNotificationsPermissionState = postNotificationsPermissionState.state)

    OnChange(value = permissionCards) {
        if (it.isEmpty()) {
            navigator.navigate(
                direction = HomeScreenDestination,
                builder = {
                    launchSingleTop = true
                    popUpTo(HomeScreenDestination)
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBarAboveHorizontalDivider(title = stringResource(id = R.string.required_permissions))
        }
    ) { paddingValues ->
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
private fun PortraitMode(
    permissionCards: ImmutableList<ModifierReceivingComposable>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        permissionCards.forEach {
            it(Modifier)
        }
    }
}

@Composable
private fun LandscapeMode(
    permissionCards: ImmutableList<ModifierReceivingComposable>,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        permissionCards.forEach {
            it(
                Modifier
                    .fillMaxWidth(0.4f)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberMovablePermissionCards(
    postNotificationsPermissionState: PermissionState?,
    appVM: AppViewModel = activityViewModel(),
    context: Context = LocalContext.current
): ImmutableList<ModifierReceivingComposable> {
    val manageAllFilesPermissionGranted by appVM.manageAllFilesPermissionGranted.collectAsStateWithLifecycle()

    return remember(
        key1 = postNotificationsPermissionState?.status?.isGranted,
        key2 = manageAllFilesPermissionGranted
    ) {
        buildList {
            if (postNotificationsPermissionState?.status?.isGranted == false) {
                add(
                    PermissionCardProperties(
                        iconRes = R.drawable.ic_notifications_24,
                        textRes = R.string.post_notifications_permission_rational,
                        onGrantButtonClick = {
                            postNotificationsPermissionState.launchPermissionRequest(
                                launchedBefore = appVM.postNotificationsPermissionRequested.value,
                                onSuppressed = {
                                    goToAppSettings(
                                        context
                                    )
                                }
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
                        onGrantButtonClick = {
                            goToManageExternalStorageSettings(context)
                        }
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