package com.w2sv.filenavigator.ui.screens.navigatorsettings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.RightAlignedSwitch
import com.w2sv.filenavigator.ui.sharedviewmodels.NavigatorViewModel
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.ConfigurationChangeConfirmationButtons
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection.FileTypeSelectionColumn

@Composable
fun NavigatorSettingsScreen(
    modifier: Modifier = Modifier,
    navigatorVM: NavigatorViewModel = viewModel()
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceEvenly) {
        FileTypeSelectionColumn(
            fileTypesState = navigatorVM.fileTypesState,
            modifier = Modifier.fillMaxHeight(0.6f)
        )
        SwitchItemRow(
            iconRes = R.drawable.ic_battery_low_24,
            textRes = R.string.disable_on_low_battery,
            checked = navigatorVM.disableOnLowBattery.collectAsState().value,
            onCheckedChange = navigatorVM::saveDisableOnLowBattery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
        AnimatedVisibility(visible = navigatorVM.fileTypesState.statesDissimilar.collectAsState().value) {
            ConfigurationChangeConfirmationButtons(
                navigatorVM.fileTypesState::reset,
                navigatorVM.fileTypesState::launchSync
            )
        }
    }
}

@Composable
fun SwitchItemRow(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        AppFontText(text = stringResource(id = textRes))
        RightAlignedSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}