package com.w2sv.filenavigator.ui.screens.navigatorsettings.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.states.FileTypesState
import com.w2sv.filenavigator.ui.theme.AppColor

@Composable
internal fun ConfigurationChangeConfirmationButtons(
    onDiscardButtonPress: () -> Unit,
    onConfirmButtonPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        ConfigurationChangeButton(
            iconRes = com.w2sv.navigator.R.drawable.ic_cancel_24,
            color = AppColor.error,
            textRes = R.string.discard_changes,
            onClick = onDiscardButtonPress
        )

        Spacer(modifier = Modifier.width(10.dp))

        ConfigurationChangeButton(
            iconRes = R.drawable.ic_check_24,
            color = AppColor.success,
            textRes = R.string.confirm_changes,
            onClick = onConfirmButtonPress
        )
    }
}

@Composable
private fun ConfigurationChangeButton(
    @DrawableRes iconRes: Int,
    color: Color,
    @StringRes textRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = color
            )
            AppFontText(text = stringResource(id = textRes))
        }
    }
}