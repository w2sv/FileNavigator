package com.w2sv.filenavigator.ui.screens.missingpermissions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.DialogButton
import com.w2sv.filenavigator.ui.components.styledTextResource
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.theme.DefaultElevatedCardElevation

@Preview
@Composable
private fun PermissionCardPrev() {
    AppTheme {
        PermissionCard(
            PermissionCardProperties(
                iconRes = R.drawable.ic_notifications_24,
                textRes = R.string.post_notifications_permission_rational,
                onGrantButtonClick = { /*TODO*/ }
            )
        )
    }
}

data class PermissionCardProperties(
    @DrawableRes val iconRes: Int,
    @StringRes val textRes: Int,
    val onGrantButtonClick: () -> Unit
)

@Composable
fun PermissionCard(
    properties: PermissionCardProperties,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = DefaultElevatedCardElevation)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = properties.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppFontText(
                text = styledTextResource(id = properties.textRes),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            DialogButton(
                text = stringResource(id = R.string.grant),
                onClick = properties.onGrantButtonClick,
            )
        }
    }
}