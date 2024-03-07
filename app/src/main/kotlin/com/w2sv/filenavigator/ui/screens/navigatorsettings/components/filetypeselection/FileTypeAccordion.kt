package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.states.NavigatorConfiguration
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.utils.orDisabledIf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FileTypeAccordion(
    fileType: FileType,
    isFirstDisabled: () -> Boolean,
    navigatorConfiguration: NavigatorConfiguration,
    modifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
) {
    Column(
        modifier = modifier
    ) {
        if (isFirstDisabled()) {
            DisabledText(modifier = Modifier.padding(bottom = 8.dp))
        }

        val isEnabled =
            navigatorConfiguration.statusMap.getValue(fileType)

        Header(
            fileType = fileType,
            isEnabled = isEnabled,
            onCheckedChange = { checkedNew ->
                navigatorConfiguration.onFileTypeCheckedChangeInput(
                    fileType = fileType,
                    checkedNew = checkedNew,
                    showSnackbar = { visuals ->
                        scope.launch {
                            snackbarHostState.dismissCurrentSnackbarAndShow(
                                visuals
                            )
                        }
                    },
                    context = context
                )
            }
        )
        AnimatedVisibility(
            visible = isEnabled
        ) {
            FileTypeSourcesSurface(
                fileType = fileType,
                navigatorConfiguration = navigatorConfiguration
            )
        }
    }
}

@Composable
private fun DisabledText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.disabled),
        fontSize = 16.sp,
        color = AppColor.disabled,
        modifier = modifier
    )
}

@Composable
private fun Header(
    fileType: FileType,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = fileType.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = fileType.color.orDisabledIf(condition = !isEnabled)
                )
            }
            Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = stringResource(id = fileType.titleRes),
                    fontSize = 18.sp,
                    color = Color.Unspecified.orDisabledIf(condition = !isEnabled)
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Switch(
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(8.dp),
                    checked = isEnabled,
                    onCheckedChange = onCheckedChange
                )
            }
        }
    }
}
