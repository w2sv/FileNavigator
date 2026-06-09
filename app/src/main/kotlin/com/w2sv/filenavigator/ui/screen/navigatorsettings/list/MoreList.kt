package com.w2sv.filenavigator.ui.screen.navigatorsettings.list

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.designsystem.component.Icon
import com.w2sv.designsystem.component.ItemLayout
import com.w2sv.designsystem.component.SwitchItemLayout
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.NavigatorConfigActions
import com.w2sv.modules.resources.R

private enum class MoreListItem(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    @StringRes val explanationRes: Int? = null,
    val checked: (NavigatorConfig) -> Boolean,
    val onCheckedChange: NavigatorConfigActions.(Boolean) -> Unit
) {
    ShowBatchMoveNotification(
        iconRes = R.drawable.ic_files_24,
        labelRes = R.string.show_batch_move_notification,
        explanationRes = R.string.batch_move_explanation,
        checked = { it.showBatchMoveNotification },
        onCheckedChange = { checked -> update { it.copy(showBatchMoveNotification = checked) } }
    ),
    DisableOnLowBattery(
        iconRes = R.drawable.ic_battery_low_24,
        labelRes = R.string.disable_navigator_on_low_battery,
        checked = { it.disableOnLowBattery },
        onCheckedChange = { checked -> update { it.copy(disableOnLowBattery = checked) } }
    ),
    StartOnBoot(
        iconRes = R.drawable.ic_restart_24,
        labelRes = R.string.start_navigator_on_system_boot,
        checked = { it.startOnBoot },
        onCheckedChange = { checked -> update { it.copy(startOnBoot = checked) } }
    )
}

fun LazyListScope.moreItemList(config: NavigatorConfig, actions: NavigatorConfigActions) {
    item {
        NavigatorSettingsListSectionHeader(
            text = stringResource(id = R.string.more),
            padding = NavigatorSettingsListDimens.moreSectionHeaderPadding
        )
    }
    val itemRowModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp)
        .padding(bottom = 2.dp)

    if (isRequestQuickSettingsTileSupported) {
        item {
            ItemLayout(
                icon = { Icon(R.drawable.ic_tile_small_24) },
                labelRes = R.string.quick_settings_tile,
                modifier = itemRowModifier
            ) {
                ElevatedButton(onClick = rememberRequestAddFileNavigatorTile()) {
                    Text(stringResource(R.string.add))
                }
            }
        }
    }
    items(MoreListItem.entries, key = { it }) { item ->
        SwitchItemLayout(
            icon = { Icon(res = item.iconRes) },
            labelRes = item.labelRes,
            checked = item.checked(config),
            onCheckedChange = { item.onCheckedChange(actions, it) },
            explanation = item.explanationRes?.let { stringResource(it) },
            modifier = itemRowModifier
        )
    }
}
