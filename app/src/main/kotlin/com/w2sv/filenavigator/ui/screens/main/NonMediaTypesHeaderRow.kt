package com.w2sv.filenavigator.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.theme.RailwayText

@Composable
fun NonMediaTypesHeaderRow(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.secondary.copy(0.7f)

    var showInfoDialog by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                NonMediaTypeInfoDialog(onDismissRequest = {
                    value = false
                })
            }
        }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.CenterStart) {
            RailwayText(
                text = stringResource(id = R.string.non_media_types),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(start = 32.dp),
                fontStyle = FontStyle.Italic,
                color = color
            )
        }
        Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
            IconButton(onClick = { showInfoDialog = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info_24),
                    contentDescription = stringResource(
                        R.string.show_a_non_media_file_type_info_dialog
                    ),
                    tint = color,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun NonMediaTypeInfoDialog(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_info_24),
                contentDescription = null,
                modifier = Modifier.size(
                    dimensionResource(id = R.dimen.dialog_icon_size)
                )
            )
        },
        text = {
            RailwayText(
                text = stringResource(R.string.non_media_type_info_text),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            ElevatedButton(onClick = onDismissRequest) {
                RailwayText(text = stringResource(R.string.got_it))
            }
        }
    )
}