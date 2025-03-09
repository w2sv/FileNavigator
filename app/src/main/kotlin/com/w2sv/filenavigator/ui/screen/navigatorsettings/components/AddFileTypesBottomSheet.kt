package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.CollectFromFlow
import com.w2sv.composed.extensions.thenIf
import com.w2sv.composed.extensions.toMutableStateMap
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.DeletionTooltip
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.designsystem.FileTypeIcon
import com.w2sv.filenavigator.ui.designsystem.rememberExtendedTooltipState
import com.w2sv.filenavigator.ui.modelext.stringResource
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.EnabledKeysTrackingSnapshotStateMap
import com.w2sv.kotlinutils.toggle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import slimber.log.i

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFileTypesBottomSheet(
    disabledFileTypes: ImmutableList<FileType>,
    addFileTypes: (List<FileType>) -> Unit,
    deleteCustomFileType: (CustomFileType) -> Unit,
    onDismissRequest: () -> Unit,
    onCreateFileTypeCardClick: () -> Unit,
    selectFileType: Flow<FileType>,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val selectionMap = remember {
        EnabledKeysTrackingSnapshotStateMap(
            disabledFileTypes
                .associateWith { false }
                .toMutableStateMap()
        )
    }

    CollectFromFlow(selectFileType) { fileType ->
        i { "Putting $fileType=true" }
        selectionMap.put(fileType, true)
    }

    val fileTypeCard: @Composable LazyGridItemScope.(FileType) -> Unit = remember {
        { fileType ->
            FileTypeCard(
                fileType = fileType,
                isSelected = selectionMap.getValue(fileType),
                onClick = { selectionMap.toggle(fileType) },
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .animateItem()
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.add_file_types),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally)
        )
        LazyVerticalGrid(
            columns = GridCells.FixedSize(92.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            items(disabledFileTypes, key = { it.ordinal }) { fileType ->
                if (fileType is CustomFileType) {
                    DeleteCustomFileTypeTooltipBox(deleteCustomFileType = { deleteCustomFileType(fileType) }) {
                        fileTypeCard(fileType)
                    }
                } else {
                    fileTypeCard(fileType)
                }
            }
            item {
                CreateFileTypeCard(
                    onClick = onCreateFileTypeCardClick,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .animateItem()
                )
            }
        }
        DialogButton(
            text = pluralStringResource(
                id = R.plurals.add_file_types_button,
                count = selectionMap.enabledKeys.size
            ),
            onClick = remember {
                {
                    addFileTypes(selectionMap.enabledKeys)
                    scope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                }
            },
            enabled = selectionMap.enabledKeys.isNotEmpty(),
            modifier = Modifier
                .padding(end = 16.dp)
                .animateContentSize()
                .padding(horizontal = 8.dp)
                .align(Alignment.End)
                .padding(bottom = 12.dp) // To prevent elevation shadow cutoff
        )
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBarsIgnoringVisibility))
    }
}

@Composable
private fun DeleteCustomFileTypeTooltipBox(deleteCustomFileType: () -> Unit, content: @Composable () -> Unit) {
    val tooltipState = rememberExtendedTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            DeletionTooltip(
                onClick = {
                    deleteCustomFileType()
                    tooltipState.dismiss()
                },
                contentDescription = stringResource(R.string.delete_file_type)
            )
        },
        state = tooltipState,
        content = content
    )
}

@Composable
private fun FileTypeCard(
    fileType: FileType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GridCard(isSelected = isSelected, onClick = onClick, modifier = modifier) {
        FileTypeIcon(fileType, modifier = Modifier.size(46.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = fileType.stringResource())
    }
}

@Composable
private fun CreateFileTypeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    GridCard(isSelected = false, onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Text("Create a file type", textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
private fun CreateFileTypeCardPrev() {
    AppTheme {
        CreateFileTypeCard({})
    }
}

@Composable
private fun GridCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .thenIf(isSelected) {
                border(
                    color = MaterialTheme.colorScheme.primary,
                    width = 3.dp,
                    shape = MaterialTheme.shapes.medium
                )
            },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(12.dp),
            content = content
        )
    }
}
