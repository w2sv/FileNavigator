package com.w2sv.filenavigator.ui.screens.main.components

import android.content.Context
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.navigator.service.FileNavigatorService
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import com.w2sv.filenavigator.ui.theme.md_negative
import com.w2sv.filenavigator.ui.theme.md_positive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun NavigatorConfigurationButtons(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {

    Row(modifier = modifier) {
        ConfigurationChangeButton(
            iconRes = R.drawable.ic_cancel_24,
            color = md_negative,
            textRes = R.string.discard_changes,
            onClick = {
                scope.launch {
                    mainScreenViewModel.unconfirmedNavigatorConfiguration.reset()
                }
            }
        )

        Spacer(modifier = Modifier.width(10.dp))

        ConfigurationChangeButton(
            iconRes = R.drawable.ic_check_24,
            md_positive,
            R.string.confirm_changes,
            onClick = {
                with(mainScreenViewModel) {
                    launchUnconfirmedFileTypeStatusSync()
                        .invokeOnCompletion {
                            // If FileListenerService is already running, relaunch with new file observer configuration
                            if (isNavigatorRunning.value) {
                                FileNavigatorService.reregisterFileObservers(
                                    context
                                )
                            }
                        }
                }
            }
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
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = color)
            AppFontText(text = stringResource(id = textRes))
        }
    }
}