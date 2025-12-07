package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.AppViewModel
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.activityViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach

@Composable
fun AutoMoveIntroductionDialogIfNotYetShown(appVM: AppViewModel = activityViewModel()) {
    val delayingFlow = remember { appVM.showAutoMoveIntroduction.onEach { delay(1_000) } }
    val showDialog by delayingFlow.collectAsStateWithLifecycle(false)

    if (showDialog) {
        AutoMoveIntroductionDialog(onDismissRequest = { appVM.saveShowAutoMoveIntroduction(false) })
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
