package com.w2sv.filenavigator.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.theme.RailwayText
import com.w2sv.filenavigator.ui.theme.md_negative
import com.w2sv.filenavigator.ui.theme.md_positive

class ExtendedSnackbarVisuals(
    override val message: String,
    val kind: SnackbarKind,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val actionLabel: String? = null,
    val action: (() -> Unit)? = null,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals

enum class SnackbarKind {
    Error,
    Success
}

suspend fun SnackbarHostState.showSnackbarAndDismissCurrentIfApplicable(snackbarVisuals: SnackbarVisuals) {
    currentSnackbarData?.dismiss()
    showSnackbar(snackbarVisuals)
}

@Composable
fun AppSnackbar(snackbarData: SnackbarData) {
    val visuals = snackbarData.visuals as ExtendedSnackbarVisuals

    Snackbar(
        action = {
            visuals.action?.let { action ->
                TextButton(onClick = {
                    action.invoke()
                }) {
                    RailwayText(text = visuals.actionLabel!!)
                }
            }
        }
    ) {
        Row {
            (snackbarData.visuals as? ExtendedSnackbarVisuals)?.run {
                when (kind) {
                    SnackbarKind.Error -> Icon(
                        painter = painterResource(id = R.drawable.ic_error_24),
                        contentDescription = null,
                        tint = md_negative
                    )

                    SnackbarKind.Success -> Icon(
                        painter = painterResource(id = R.drawable.ic_success_24),
                        contentDescription = null,
                        tint = md_positive
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            RailwayText(text = snackbarData.visuals.message)
        }
    }
}