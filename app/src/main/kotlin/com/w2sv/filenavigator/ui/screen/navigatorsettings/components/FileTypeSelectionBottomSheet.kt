package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarContent
import com.w2sv.filenavigator.ui.designsystem.DeletionTooltip
import com.w2sv.filenavigator.ui.designsystem.FileTypeIcon
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.modelext.stringResource
import com.w2sv.filenavigator.ui.theme.AppTheme
import kotlinx.collections.immutable.toImmutableMap

@Composable
fun FileTypeSelectionBottomSheet(
    state: FileTypeSelectionState,
    showFileTypeCreationDialog: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.select_file_types),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally)
        )
        AnimatedVisibility(state.showSelectionWarning) {
            AppSnackbarContent(
                snackbarKind = SnackbarKind.Error,
                message = stringResource(R.string.leave_at_least_one_file_type_selected),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            )
        }
        LazyVerticalGrid(
            columns = GridCells.FixedSize(92.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            items(state.sortedFileTypes, key = { it.ordinal }) { fileType ->
                FileTypeGridItem(fileType, state)
            }
            item {
                CreateFileTypeCard(onClick = showFileTypeCreationDialog)
            }
        }
    }
}

@Composable
private fun LazyGridItemScope.FileTypeGridItem(
    fileType: FileType,
    state: FileTypeSelectionState,
    modifier: Modifier = Modifier
) {
    val content: @Composable () -> Unit = {
        FileTypeCard(
            fileType = fileType,
            isSelected = state.selectionMap.getValue(fileType),
            onClick = { state.toggleSelectionOrShowWarning(fileType) },
            modifier = modifier
        )
    }

    if (fileType is CustomFileType) {
        DeleteCustomFileTypeTooltipBox(deleteCustomFileType = { state.deleteCustomFileType(fileType) }) {
            content()
        }
    } else {
        content()
    }
}

@Composable
private fun DeleteCustomFileTypeTooltipBox(deleteCustomFileType: () -> Unit, content: @Composable () -> Unit) {
    val tooltipState = rememberTooltipState()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            DeletionTooltip(
                onClick = {
                    deleteCustomFileType()
                    tooltipState.dismiss()
                },
                contentDescription = stringResource(R.string.delete_the_file_type)
            )
        },
        state = tooltipState,
        content = content
    )
}

@Composable
private fun LazyGridItemScope.FileTypeCard(
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
private fun LazyGridItemScope.CreateFileTypeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    GridCard(isSelected = false, onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Text(stringResource(R.string.create_file_type_card_label), textAlign = TextAlign.Center)
    }
}

@Composable
private fun LazyGridItemScope.GridCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .padding(bottom = 12.dp)
            .animateItem()
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

@Preview(showSystemUi = true)
@Composable
private fun Prev() {
    AppTheme {
        val map = NavigatorConfig.default.fileTypeConfigMap.mapValues { it.value.enabled }
        FileTypeSelectionBottomSheet(
            state = FileTypeSelectionState(
                map.toImmutableMap(),
                {},
                {},
                rememberCoroutineScope()
            ),
            showFileTypeCreationDialog = {},
            onDismissRequest = {},
            sheetState = SheetState(
                initialValue = SheetValue.Expanded,
                skipPartiallyExpanded = true,
                positionalThreshold = { 0.5f },
                velocityThreshold = { 0.5f }
            )
        )
    }
}
