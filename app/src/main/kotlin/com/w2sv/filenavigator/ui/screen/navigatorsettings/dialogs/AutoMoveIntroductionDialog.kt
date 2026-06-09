package com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.designsystem.component.DialogButton
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.modules.resources.R
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
fun AutoMoveIntroductionDialogIfNotYetShown(show: Boolean, onDismissRequest: () -> Unit) {
    val showDialog = produceState(initialValue = false, key1 = show) {
        if (show) {
            delay(1_000.milliseconds)
            value = true
        }
    }

    if (showDialog.value) {
        AutoMoveIntroductionDialog(onDismissRequest = onDismissRequest)
    }
}

@Composable
private fun AutoMoveIntroductionDialog(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.introducing_auto_move)) },
        icon = { Text(text = "🎉", fontSize = 30.sp) },
        confirmButton = {
            DialogButton(
                text = stringResource(R.string.awesome),
                onClick = onDismissRequest
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.auto_move_introduction_paragraph_1),
                    modifier = Modifier.padding(bottom = TextSectionBottomPadding)
                )
                Text(
                    text = rememberStyledTextResource(R.string.auto_move_introduction_paragraph_2),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = TextSectionBottomPadding)
                )
                Text(
                    text = stringResource(R.string.auto_move_introduction_paragraph_3)
                )
            }
        },
        modifier = modifier
    )
}

private val TextSectionBottomPadding = 6.dp

@Preview
@Composable
private fun AutoMoveIntroductionPrev() {
    AppTheme {
        AutoMoveIntroductionDialog(onDismissRequest = {})
    }
}
