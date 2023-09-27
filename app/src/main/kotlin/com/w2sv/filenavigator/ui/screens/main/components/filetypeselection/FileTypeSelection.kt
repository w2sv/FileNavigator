package com.w2sv.filenavigator.ui.screens.main.components.filetypeselection

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.data.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.states.FileTypeState
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.CascadeAnimationState
import slimber.log.i

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTypeSelectionColumn(
    modifier: Modifier = Modifier,
    fileTypeState: FileTypeState,
    context: Context = LocalContext.current
) {
    val selectDefaultMoveDestination = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            i { "DocumentTree Uri: $treeUri" }
            fileTypeState.defaultMoveDestinationState.onDestinationSelected(treeUri, context)
        }
    }

    fileTypeState.defaultMoveDestinationState.selectionSource.collectAsState()
        .apply {
            if (value != null) {
                selectDefaultMoveDestination.launch(null)
            }
        }

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

        val firstDisabledFileType by fileTypeState.firstDisabledFileType.collectAsState()

        LazyColumn(state = rememberLazyListState()) {
            items(
                items = fileTypeState.sortedFileTypes,
                key = { it }
            ) { fileType ->
                i { "Laying out ${fileType.identifier}" }

                FileTypeAccordion(
                    fileType = fileType,
                    isFirstDisabled = fileType == firstDisabledFileType,
                    fileTypeState = fileTypeState,
                    cascadeAnimationState = cascadeAnimationState,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItemPlacement(tween(DefaultAnimationDuration))  // Animate upon reordering
                )
            }
        }
    }
}