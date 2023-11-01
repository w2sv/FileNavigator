package com.w2sv.filenavigator.ui.screens.main.components.filetypeselection

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.data.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.states.FileTypesState
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.CascadeAnimationState
import slimber.log.i

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTypeSelectionColumn(
    modifier: Modifier = Modifier,
    fileTypesState: FileTypesState,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
    ) {
        AppFontText(
            text = stringResource(id = R.string.file_types),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        val cascadeAnimationState = remember {
            CascadeAnimationState<FileType>()
        }

        val firstDisabledFileType by fileTypesState.firstDisabledFileType.collectAsState()

        LazyColumn(state = rememberLazyListState()) {
            items(
                items = fileTypesState.sortedFileTypes,
                key = { it }
            ) { fileType ->
                i { "Laying out ${fileType.name}" }

                FileTypeAccordion(
                    fileType = fileType,
                    isFirstDisabled = { fileType == firstDisabledFileType },
                    fileTypesState = fileTypesState,
                    cascadeAnimationState = cascadeAnimationState,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItemPlacement(tween(DefaultAnimationDuration))  // Animate upon reordering
                )
            }
        }
    }
}