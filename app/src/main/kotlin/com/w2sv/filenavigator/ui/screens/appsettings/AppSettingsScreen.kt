package com.w2sv.filenavigator.ui.screens.appsettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.BackArrowTopAppBar
import com.w2sv.filenavigator.ui.designsystem.NavigationTransitions
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.SwitchItemRow
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.theme.onSurfaceVariantDecreasedAlpha
import com.w2sv.filenavigator.ui.utils.activityViewModel

@OptIn(ExperimentalLayoutApi::class)
@Destination<RootGraph>(style = NavigationTransitions::class)
@Composable
fun AppSettingsScreen(
    navigator: DestinationsNavigator,
    appVM: AppViewModel = activityViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)
    ) {
        BackArrowTopAppBar(
            title = stringResource(id = R.string.app_settings),
            onBack = remember { { navigator.popBackStack() } }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Padding.defaultHorizontal)
                .padding(top = 16.dp)
        ) {
            SwitchItemRow(
                iconRes = R.drawable.ic_storage_24,
                textRes = R.string.show_storage_volume_names,
                checked = appVM.showStorageVolumeNames.collectAsState().value,
                onCheckedChange = remember { { appVM.saveShowStorageVolumeNames(it) } },
            )
            Text(
                text = stringResource(R.string.show_storage_volume_names_explanation),
                color = MaterialTheme.colorScheme.onSurfaceVariantDecreasedAlpha,
                modifier = Modifier.padding(start = 24.dp + 16.dp),
                fontSize = 14.sp
            )
        }
    }
}