package com.w2sv.filenavigator.ui.screen.permissions

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.common.AppUrl
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.core.common.R
import com.w2sv.designsystem.DialogButton
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.filenavigator.ui.designsystem.AppCardDefaults

@Preview
@Composable
private fun PermissionCardPrev() {
    AppTheme {
        PermissionCard(PermissionCard.manageAllFiles {})
    }
}

@Immutable
data class PermissionCard(
    @DrawableRes val iconRes: Int,
    @StringRes val textRes: Int,
    val onGrantButtonClick: (Context) -> Unit,
    val buildSecondaryText: (AnnotatedString.Builder.(Context) -> Unit)? = null
) {
    companion object {
        fun postNotifications(onGrantButtonClick: (Context) -> Unit): PermissionCard =
            PermissionCard(
                iconRes = R.drawable.ic_notifications_24,
                textRes = R.string.post_notifications_permission_rational,
                onGrantButtonClick = onGrantButtonClick
            )

        fun manageAllFiles(onGrantButtonClick: (Context) -> Unit): PermissionCard =
            PermissionCard(
                iconRes = R.drawable.ic_folder_open_24,
                textRes = R.string.manage_external_storage_permission_rational,
                onGrantButtonClick = onGrantButtonClick,
                buildSecondaryText = { context ->
                    withLink(LinkAnnotation.Url(AppUrl.PRIVACY_POLICY)) {
                        append(context.getString(R.string.privacy_policy))
                    }
                }
            )
    }
}

@Composable
fun PermissionCard(card: PermissionCard, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = modifier,
        elevation = AppCardDefaults.elevation
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = card.iconRes),
                contentDescription = null,
                tint = colorScheme.secondary,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .size(28.dp)
            )
            Text(
                text = rememberStyledTextResource(id = card.textRes),
                textAlign = TextAlign.Center
            )
            card.buildSecondaryText?.let { build ->
                Text(
                    text = buildAnnotatedString { build(context) },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            DialogButton(
                text = stringResource(id = R.string.grant),
                onClick = { card.onGrantButtonClick(context) },
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
