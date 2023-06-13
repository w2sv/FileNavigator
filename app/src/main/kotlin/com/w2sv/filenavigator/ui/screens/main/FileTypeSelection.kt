package com.w2sv.filenavigator.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.mediastore.FileType
import com.w2sv.filenavigator.ui.ExtendedSnackbarVisuals
import com.w2sv.filenavigator.ui.SnackbarKind
import com.w2sv.filenavigator.ui.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
import com.w2sv.filenavigator.ui.theme.RailwayText
import com.w2sv.filenavigator.ui.theme.md_positive
import com.w2sv.filenavigator.utils.toggle
import kotlinx.coroutines.launch

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

@Composable
fun FileTypeAccordionColumn(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val verticalPaddingModifier = Modifier.padding(vertical = 4.dp)

        FileType.Media.all.forEach {
            FileTypeAccordion(fileType = it, modifier = verticalPaddingModifier)
        }
        NonMediaTypesHeaderRow()
        FileType.NonMedia.all.forEach {
            FileTypeAccordion(fileType = it, modifier = verticalPaddingModifier)
        }
    }
}

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
private fun FileTypeAccordion(
    fileType: FileType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    Column(modifier = modifier) {
        AccordionHeader(fileType = fileType)
        if (fileType is FileType.Media) {
            AnimatedVisibility(visible = mainScreenViewModel.accountForFileType.getValue(fileType)) {
                AccordionCorpus(fileType = fileType)
            }
        }
    }
}

@Composable
private fun AccordionHeader(
    fileType: FileType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(tonalElevation = 2.dp, shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = fileType.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = fileType.color
                )
            }
            Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.CenterStart) {
                RailwayText(
                    text = stringResource(id = fileType.titleRes),
                    fontSize = 18.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Switch(
                    modifier = Modifier.padding(8.dp),
                    checked = mainScreenViewModel.accountForFileType.getValue(fileType),
                    onCheckedChange = { checkedNew ->
                        if (mainScreenViewModel.accountForFileType.values.atLeastOneTrueAfterValueChange(
                                checkedNew
                            )
                        ) {
                            mainScreenViewModel.accountForFileType.toggle(
                                fileType
                            )
                        } else {
                            scope.launch {
                                mainScreenViewModel.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                    ExtendedSnackbarVisuals(
                                        message = context.getString(
                                            R.string.leave_at_least_one_file_type_enabled
                                        ),
                                        kind = SnackbarKind.Error
                                    )
                                )
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = md_positive)
                )
            }
        }
    }
}

@Composable
private fun AccordionCorpus(
    fileType: FileType,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            fileType.sources.forEachIndexed { i, origin ->
                FileTypeOriginRow(fileType = fileType, source = origin)
                if (i != fileType.sources.lastIndex) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun FileTypeOriginRow(
    fileType: FileType,
    source: FileType.Source,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        val nestedEntryColor = MaterialTheme.colorScheme.secondary.copy(0.7f)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right_24),
                    contentDescription = null,
                    tint = nestedEntryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Box(modifier = Modifier.weight(0.12f), contentAlignment = Alignment.CenterStart) {
                Icon(
                    painter = painterResource(id = source.kind.iconRes),
                    contentDescription = null,
                    tint = fileType.color.copy(alpha = 0.6f)
                )
            }
            Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.CenterStart) {
                RailwayText(
                    text = stringResource(id = source.kind.labelRes),
                    color = nestedEntryColor
                )
            }
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                Checkbox(
                    checked = mainScreenViewModel.accountForFileTypeSource.getValue(source),
                    onCheckedChange = { checkedNew ->
                        when (fileType.sources.map {
                            mainScreenViewModel.accountForFileTypeSource.getValue(
                                it
                            )
                        }
                            .atLeastOneTrueAfterValueChange(checkedNew)
                        ) {
                            true -> mainScreenViewModel.accountForFileTypeSource[source] =
                                checkedNew

                            false -> scope.launch {
                                mainScreenViewModel.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                    ExtendedSnackbarVisuals(
                                        message = context.getString(
                                            R.string.leave_at_least_one_file_source_selected_or_disable_the_entire_file_type,
                                            context.getString(fileType.titleRes)
                                        ),
                                        kind = SnackbarKind.Error
                                    )
                                )
                            }
                        }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = md_positive)
                )
            }
        }
    }
}

@Preview
@Composable
private fun HeaderPrev() {
    FileNavigatorTheme {
        AccordionHeader(fileType = FileType.Image)
    }
}

fun Iterable<Boolean>.atLeastOneTrueAfterValueChange(newValue: Boolean): Boolean =
    newValue || count { it } > 1