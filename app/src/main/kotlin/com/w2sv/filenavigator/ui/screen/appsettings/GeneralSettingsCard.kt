package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.filenavigator.ui.designsystem.AppCard
import com.w2sv.filenavigator.ui.designsystem.SwitchItemLayout
import com.w2sv.modules.common.R

@Composable
fun GeneralSettingsCard(showStorageVolumeNames: Boolean, onShowStorageVolumeNamesChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        title = stringResource(R.string.general),
        modifier = modifier
    ) {
        SwitchItemLayout(
            icon = { SettingsItemIcon(res = R.drawable.ic_storage_24) },
            labelRes = R.string.show_storage_volume_names,
            checked = showStorageVolumeNames,
            onCheckedChange = onShowStorageVolumeNamesChange,
            explanation = rememberStyledTextResource(R.string.show_storage_volume_names_explanation)
        )
    }
}
