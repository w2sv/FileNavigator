package com.w2sv.filenavigator.ui.screen.appsettings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.filenavigator.ui.designsystem.AppCard
import com.w2sv.filenavigator.ui.designsystem.ItemLayout
import com.w2sv.filenavigator.ui.screen.appsettings.model.SettingsActionGroup

@Composable
fun SettingsActionGroupCard(group: SettingsActionGroup, context: Context, modifier: Modifier = Modifier) {
    AppCard(
        title = stringResource(group.titleRes),
        modifier = modifier
    ) {
        Column(verticalArrangement = AppSettingsScreenDimens.cardActionsVerticalArrangement) {
            group.actions.forEach { action ->
                ItemLayout(
                    icon = { SettingsItemIcon(res = action.iconRes) },
                    labelRes = action.labelRes,
                    explanation = action.explanationRes?.let { stringResource(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { action.onClick(context) }
                )
            }
        }
    }
}
